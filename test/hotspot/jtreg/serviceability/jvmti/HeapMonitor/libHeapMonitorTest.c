/*
 * Copyright (c) 2018, Google and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "jvmti.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifndef JNI_ENV_ARG

#ifdef __cplusplus
#define JNI_ENV_ARG(x, y) y
#define JNI_ENV_PTR(x) x
#else
#define JNI_ENV_ARG(x,y) x, y
#define JNI_ENV_PTR(x) (*x)
#endif

#endif

#define TRUE 1
#define FALSE 0
#define PRINT_OUT 0

static jvmtiEnv *jvmti = NULL;
static jvmtiEnv *second_jvmti = NULL;

typedef struct _ObjectTrace{
  jweak object;
  jlong size;
  jvmtiFrameInfo* frames;
  size_t frame_count;
  jthread thread;
} ObjectTrace;

typedef struct _EventStorage {
  int live_object_additions;
  int live_object_size;
  int live_object_count;
  ObjectTrace** live_objects;

  int garbage_history_size;
  int garbage_history_index;
  ObjectTrace** garbage_collected_objects;

  // Two separate monitors to separate storage data race and the compaction field
  // data race.
  jrawMonitorID storage_monitor;

  int compaction_required;
  jrawMonitorID compaction_monitor;
} EventStorage;

typedef struct _ExpectedContentFrame {
  const char *name;
  const char *signature;
  const char *file_name;
  int line_number;
} ExpectedContentFrame;

static
void event_storage_lock(EventStorage* storage) {
  (*jvmti)->RawMonitorEnter(jvmti, storage->storage_monitor);
}

static
void event_storage_unlock(EventStorage* storage) {
  (*jvmti)->RawMonitorExit(jvmti, storage->storage_monitor);
}

static
void event_storage_lock_compaction(EventStorage* storage) {
  (*jvmti)->RawMonitorEnter(jvmti, storage->compaction_monitor);
}

static
void event_storage_unlock_compaction(EventStorage* storage) {
  (*jvmti)->RawMonitorExit(jvmti, storage->compaction_monitor);
}

// Given a method and a location, this method gets the line number.
static
jint get_line_number(jvmtiEnv* jvmti, jmethodID method,
                     jlocation location) {
  // Read the line number table.
  jvmtiLineNumberEntry *table_ptr = 0;
  jint line_number_table_entries;
  int l;
  jlocation last_location;
  int jvmti_error = (*jvmti)->GetLineNumberTable(jvmti, method,
                                                 &line_number_table_entries,
                                                 &table_ptr);

  if (JVMTI_ERROR_NONE != jvmti_error) {
    return -1;
  }
  if (line_number_table_entries <= 0) {
    return -1;
  }
  if (line_number_table_entries == 1) {
    return table_ptr[0].line_number;
  }

  // Go through all the line numbers...
  last_location = table_ptr[0].start_location;
  for (l = 1; l < line_number_table_entries; l++) {
    // ... and if you see one that is in the right place for your
    // location, you've found the line number!
    if ((location < table_ptr[l].start_location) &&
        (location >= last_location)) {
      return table_ptr[l - 1].line_number;
    }
    last_location = table_ptr[l].start_location;
  }

  if (location >= last_location) {
    return table_ptr[line_number_table_entries - 1].line_number;
  } else {
    return -1;
  }
}

static void print_out_frames(JNIEnv* env, ObjectTrace* trace) {
  jvmtiFrameInfo* frames = trace->frames;
  size_t i;
  for (i = 0; i < trace->frame_count; i++) {
    // Get basic information out of the trace.
    jlocation bci = frames[i].location;
    jmethodID methodid = frames[i].method;
    char *name = NULL, *signature = NULL, *file_name = NULL;
    jclass declaring_class;
    int line_number;
    jvmtiError err;

    if (bci < 0) {
      fprintf(stderr, "\tNative frame\n");
      continue;
    }

    // Transform into usable information.
    line_number = get_line_number(jvmti, methodid, bci);
    if (JVMTI_ERROR_NONE !=
        (*jvmti)->GetMethodName(jvmti, methodid, &name, &signature, 0)) {
      fprintf(stderr, "\tUnknown method name\n");
      continue;
    }

    if (JVMTI_ERROR_NONE !=
        (*jvmti)->GetMethodDeclaringClass(jvmti, methodid, &declaring_class)) {
      fprintf(stderr, "\tUnknown class\n");
      continue;
    }

    err = (*jvmti)->GetSourceFileName(jvmti, declaring_class,
                                      &file_name);
    if (err != JVMTI_ERROR_NONE) {
      fprintf(stderr, "\tUnknown file\n");
      continue;
    }

    // Compare now, none should be NULL.
    if (name == NULL) {
      fprintf(stderr, "\tUnknown name\n");
      continue;
    }

    if (file_name == NULL) {
      fprintf(stderr, "\tUnknown file\n");
      continue;
    }

    if (signature == NULL) {
      fprintf(stderr, "\tUnknown signature\n");
      continue;
    }

    fprintf(stderr, "\t%s%s (%s: %d)\n",
            name, signature, file_name, line_number);
  }
}

static jboolean check_sample_content(JNIEnv* env,
                                     ObjectTrace* trace,
                                     ExpectedContentFrame *expected,
                                     size_t expected_count,
                                     int print_out_comparisons) {
  jvmtiFrameInfo* frames;
  size_t i;

  if (expected_count > trace->frame_count) {
    return FALSE;
  }

  frames = trace->frames;
  for (i = 0; i < expected_count; i++) {
    // Get basic information out of the trace.
    jlocation bci = frames[i].location;
    jmethodID methodid = frames[i].method;
    char *name = NULL, *signature = NULL, *file_name = NULL;
    jclass declaring_class;
    int line_number;
    jvmtiError err;

    if (bci < 0 && expected[i].line_number != -1) {
      return FALSE;
    }

    // Transform into usable information.
    line_number = get_line_number(jvmti, methodid, bci);
    (*jvmti)->GetMethodName(jvmti, methodid, &name, &signature, 0);

    if (JVMTI_ERROR_NONE !=
        (*jvmti)->GetMethodDeclaringClass(jvmti, methodid, &declaring_class)) {
      return FALSE;
    }

    err = (*jvmti)->GetSourceFileName(jvmti, declaring_class,
                                      &file_name);
    if (err != JVMTI_ERROR_NONE) {
      return FALSE;
    }

    // Compare now, none should be NULL.
    if (name == NULL) {
      return FALSE;
    }

    if (file_name == NULL) {
      return FALSE;
    }

    if (signature == NULL) {
      return FALSE;
    }

    if (print_out_comparisons) {
      fprintf(stderr, "\tComparing:\n");
      fprintf(stderr, "\t\tNames: %s and %s\n", name, expected[i].name);
      fprintf(stderr, "\t\tSignatures: %s and %s\n", signature, expected[i].signature);
      fprintf(stderr, "\t\tFile name: %s and %s\n", file_name, expected[i].file_name);
      fprintf(stderr, "\t\tLines: %d and %d\n", line_number, expected[i].line_number);
      fprintf(stderr, "\t\tResult is %d\n",
              (strcmp(name, expected[i].name) ||
               strcmp(signature, expected[i].signature) ||
               strcmp(file_name, expected[i].file_name) ||
               line_number != expected[i].line_number));
    }

    if (strcmp(name, expected[i].name) ||
        strcmp(signature, expected[i].signature) ||
        strcmp(file_name, expected[i].file_name) ||
        line_number != expected[i].line_number) {
      return FALSE;
    }
  }

  return TRUE;
}

// Static native API for various tests.
static void fill_native_frames(JNIEnv* env, jobjectArray frames,
                               ExpectedContentFrame* native_frames, size_t size) {
  size_t i;
  for (i = 0; i < size; i++) {
    jobject obj = (*env)->GetObjectArrayElement(env, frames, (jsize) i);
    jclass frame_class = (*env)->GetObjectClass(env, obj);
    jfieldID line_number_field_id = (*env)->GetFieldID(env, frame_class,
                                                       "lineNumber", "I");
    int line_number = (*env)->GetIntField(env, obj, line_number_field_id);

    jfieldID string_id = (*env)->GetFieldID(env, frame_class, "method",
                                            "Ljava/lang/String;");
    jstring string_object = (jstring) (*env)->GetObjectField(env, obj,
                                                             string_id);
    const char* method = (*env)->GetStringUTFChars(env, string_object, 0);
    const char* file_name;
    const char* signature;

    string_id = (*env)->GetFieldID(env, frame_class, "fileName",
                                   "Ljava/lang/String;");
    string_object = (jstring) (*env)->GetObjectField(env, obj, string_id);
    file_name = (*env)->GetStringUTFChars(env, string_object, 0);

    string_id = (*env)->GetFieldID(env, frame_class, "signature",
                                   "Ljava/lang/String;");
    string_object = (jstring) (*env)->GetObjectField(env, obj, string_id);
    signature= (*env)->GetStringUTFChars(env, string_object, 0);

    native_frames[i].name = method;
    native_frames[i].file_name = file_name;
    native_frames[i].signature = signature;
    native_frames[i].line_number = line_number;
  }
}

// Internal storage system implementation.
static EventStorage global_event_storage;
static EventStorage second_global_event_storage;

static void event_storage_set_compaction_required(EventStorage* storage) {
  event_storage_lock_compaction(storage);
  storage->compaction_required = 1;
  event_storage_unlock_compaction(storage);
}

static int event_storage_get_compaction_required(EventStorage* storage) {
  int result;
  event_storage_lock_compaction(storage);
  result = storage->compaction_required;
  event_storage_unlock_compaction(storage);
  return result;
}

static void event_storage_set_garbage_history(EventStorage* storage, int value) {
  size_t size;
  event_storage_lock(storage);
  global_event_storage.garbage_history_size = value;
  free(global_event_storage.garbage_collected_objects);
  size = sizeof(*global_event_storage.garbage_collected_objects) * value;
  global_event_storage.garbage_collected_objects = malloc(size);
  memset(global_event_storage.garbage_collected_objects, 0, size);
  event_storage_unlock(storage);
}

// No mutex here, it is handled by the caller.
static void event_storage_add_garbage_collected_object(EventStorage* storage,
                                                       ObjectTrace* object) {
  int idx = storage->garbage_history_index;
  ObjectTrace* old_object = storage->garbage_collected_objects[idx];
  if (old_object != NULL) {
    free(old_object->frames);
    free(storage->garbage_collected_objects[idx]);
  }

  storage->garbage_collected_objects[idx] = object;
  storage->garbage_history_index = (idx + 1) % storage->garbage_history_size;
}

static int event_storage_get_count(EventStorage* storage) {
  int result;
  event_storage_lock(storage);
  result = storage->live_object_count;
  event_storage_unlock(storage);
  return result;
}

static double event_storage_get_average_rate(EventStorage* storage) {
  double accumulation = 0;
  int max_size;
  int i;

  event_storage_lock(storage);
  max_size = storage->live_object_count;

  for (i = 0; i < max_size; i++) {
    accumulation += storage->live_objects[i]->size;
  }

  event_storage_unlock(storage);
  return accumulation / max_size;
}

static jboolean event_storage_contains(JNIEnv* env,
                                       EventStorage* storage,
                                       ExpectedContentFrame* frames,
                                       size_t size) {
  int i;
  event_storage_lock(storage);
  fprintf(stderr, "Checking storage count %d\n", storage->live_object_count);
  for (i = 0; i < storage->live_object_count; i++) {
    ObjectTrace* trace = storage->live_objects[i];

    if (check_sample_content(env, trace, frames, size, PRINT_OUT)) {
      event_storage_unlock(storage);
      return TRUE;
    }
  }
  event_storage_unlock(storage);
  return FALSE;
}

static jboolean event_storage_garbage_contains(JNIEnv* env,
                                               EventStorage* storage,
                                               ExpectedContentFrame* frames,
                                               size_t size) {
  int i;
  event_storage_lock(storage);
  fprintf(stderr, "Checking garbage storage count %d\n",
          storage->garbage_history_size);
  for (i = 0; i < storage->garbage_history_size; i++) {
    ObjectTrace* trace = storage->garbage_collected_objects[i];

    if (trace == NULL) {
      continue;
    }

    if (check_sample_content(env, trace, frames, size, PRINT_OUT)) {
      event_storage_unlock(storage);
      return TRUE;
    }
  }
  event_storage_unlock(storage);
  return FALSE;
}

// No mutex here, handled by the caller.
static void event_storage_augment_storage(EventStorage* storage) {
  int new_max = (storage->live_object_size * 2) + 1;
  ObjectTrace** new_objects = malloc(new_max * sizeof(*new_objects));

  int current_count = storage->live_object_count;
  memcpy(new_objects, storage->live_objects, current_count * sizeof(*new_objects));
  free(storage->live_objects);
  storage->live_objects = new_objects;
  storage->live_object_size = new_max;
}

static void event_storage_add(EventStorage* storage,
                              JNIEnv* jni,
                              jthread thread,
                              jobject object,
                              jclass klass,
                              jlong size) {
  jvmtiFrameInfo frames[64];
  jint count;
  jvmtiError err;

  err = (*jvmti)->GetStackTrace(jvmti, thread, 0, 64, frames, &count);
  if (err == JVMTI_ERROR_NONE && count >= 1) {
    ObjectTrace* live_object;
    jvmtiFrameInfo* allocated_frames = (jvmtiFrameInfo*) malloc(count * sizeof(*allocated_frames));
    memcpy(allocated_frames, frames, count * sizeof(*allocated_frames));

    live_object = (ObjectTrace*) malloc(sizeof(*live_object));
    live_object->frames = allocated_frames;
    live_object->frame_count = count;
    live_object->size = size;
    live_object->thread = thread;
    live_object->object = (*jni)->NewWeakGlobalRef(jni, object);

    // Only now lock and get things done quickly.
    event_storage_lock(storage);

    storage->live_object_additions++;

    if (storage->live_object_count >= storage->live_object_size) {
      event_storage_augment_storage(storage);
    }
    assert(storage->live_object_count < storage->live_object_size);

    if (PRINT_OUT) {
      fprintf(stderr, "Adding trace for thread %p, frame_count %d, storage %p\n",
              thread, count, storage);
      print_out_frames(jni, live_object);
    }
    storage->live_objects[storage->live_object_count] = live_object;
    storage->live_object_count++;

    event_storage_unlock(storage);
  }
}

static void event_storage_compact(EventStorage* storage, JNIEnv* jni) {
  int max, i, dest;
  ObjectTrace** live_objects;

  event_storage_lock_compaction(storage);
  storage->compaction_required = 0;
  event_storage_unlock_compaction(storage);

  event_storage_lock(storage);

  max = storage->live_object_count;
  live_objects = storage->live_objects;

  for (i = 0, dest = 0; i < max; i++) {
    ObjectTrace* live_object = live_objects[i];
    jweak object = live_object->object;

    if (!(*jni)->IsSameObject(jni, object, NULL)) {
      if (dest != i) {
        live_objects[dest] = live_object;
        dest++;
      }
    } else {
      (*jni)->DeleteWeakGlobalRef(jni, object);
      live_object->object = NULL;

      event_storage_add_garbage_collected_object(storage, live_object);
    }
  }

  storage->live_object_count = dest;
  event_storage_unlock(storage);
}

static void event_storage_free_objects(ObjectTrace** array, int max) {
  int i;
  for (i = 0; i < max; i++) {
    free(array[i]), array[i] = NULL;
  }
}

static void event_storage_reset(EventStorage* storage) {
  event_storage_lock(storage);

  // Reset everything except the mutex and the garbage collection.
  event_storage_free_objects(storage->live_objects,
                             storage->live_object_count);
  storage->live_object_additions = 0;
  storage->live_object_size = 0;
  storage->live_object_count = 0;
  free(storage->live_objects), storage->live_objects = NULL;

  event_storage_free_objects(storage->garbage_collected_objects,
                             storage->garbage_history_size);

  storage->compaction_required = 0;
  storage->garbage_history_index = 0;

  event_storage_unlock(storage);
}

static int event_storage_number_additions(EventStorage* storage) {
  int result;
  event_storage_lock(storage);
  result = storage->live_object_additions;
  event_storage_unlock(storage);
  return result;
}

// Start of the JVMTI agent code.
static const char *EXC_CNAME = "java/lang/Exception";

static int check_error(jvmtiError err, const char *s) {
  if (err != JVMTI_ERROR_NONE) {
    printf("  ## %s error: %d\n", s, err);
    return 1;
  }
  return 0;
}

static int check_capability_error(jvmtiError err, const char *s) {
  if (err != JVMTI_ERROR_NONE) {
    if (err == JVMTI_ERROR_MUST_POSSESS_CAPABILITY) {
      return 0;
    }
    fprintf(stderr, "  ## %s error: %d\n", s, err);
  }
  return 1;
}

static jint throw_exception(JNIEnv *env, char *msg) {
  jclass exc_class = JNI_ENV_PTR(env)->FindClass(JNI_ENV_ARG(env, EXC_CNAME));

  if (exc_class == NULL) {
    fprintf(stderr, "throw_exception: Error in FindClass(env, %s)\n",
            EXC_CNAME);
    return -1;
  }
  return JNI_ENV_PTR(env)->ThrowNew(JNI_ENV_ARG(env, exc_class), msg);
}

static jint Agent_Initialize(JavaVM *jvm, char *options, void *reserved);

JNIEXPORT
jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
  return Agent_Initialize(jvm, options, reserved);
}

JNIEXPORT
jint JNICALL Agent_OnAttach(JavaVM *jvm, char *options, void *reserved) {
  return Agent_Initialize(jvm, options, reserved);
}

JNIEXPORT
jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
  return JNI_VERSION_1_8;
}

#define MAX_THREADS 500

typedef struct ThreadStats {
  int number_threads;
  int counts[MAX_THREADS];
  int not_helper_counts[MAX_THREADS];
  int index[MAX_THREADS];
  jthread threads[MAX_THREADS];

  int method_resolution_problem;
} ThreadStats;

static ThreadStats thread_stats;

static void add_thread_count(jthread thread, int lock, int helper) {
  int i;
  jvmtiThreadInfo info;
  const char* name;
  char* end;
  int idx;
  int err;

  if (lock) {
    event_storage_lock(&global_event_storage);
  }

  for (i = 0; i < thread_stats.number_threads; i++) {
    if (thread_stats.threads[i] == thread) {
      if (helper) {
        thread_stats.counts[i]++;
      } else {
        thread_stats.not_helper_counts[i]++;
      }

      if (lock) {
        event_storage_unlock(&global_event_storage);
      }
      return;
    }
  }

  thread_stats.threads[thread_stats.number_threads] = thread;

  err = (*jvmti)->GetThreadInfo(jvmti, thread, &info);
  if (err != JVMTI_ERROR_NONE) {
    if (lock) {
      event_storage_unlock(&global_event_storage);
    }

    // Just to have it accounted as an error...
    info.name = "Allocator99";
  }

  if (!strstr(info.name, "Allocator")) {
    if (lock) {
      event_storage_unlock(&global_event_storage);
    }

    // Just to have it accounted as an error...
    info.name = "Allocator98";
  }

  name = info.name + 9;
  end = NULL;
  idx = strtol(name, &end, 0);

  if (*end == '\0') {
    if (helper) {
      thread_stats.counts[thread_stats.number_threads]++;
    } else {
      thread_stats.not_helper_counts[thread_stats.number_threads]++;
    }

    thread_stats.index[thread_stats.number_threads] = idx;
    thread_stats.number_threads++;
  } else {
    fprintf(stderr, "Problem with thread name...: %p %s\n", thread, name);
  }

  if (PRINT_OUT) {
    fprintf(stderr, "Added %s - %p - %d - lock: %d\n", info.name, thread, idx, lock);
  }

  if (lock) {
    event_storage_unlock(&global_event_storage);
  }
}

static void print_thread_stats() {
  int i;
  event_storage_lock(&global_event_storage);
  fprintf(stderr, "Method resolution problem: %d\n", thread_stats.method_resolution_problem);
  fprintf(stderr, "Thread count:\n");
  for (i = 0; i < thread_stats.number_threads; i++) {
    fprintf(stderr, "\t%p: %d: %d - %d\n", thread_stats.threads[i],
            thread_stats.index[i],
            thread_stats.counts[i],
            thread_stats.not_helper_counts[i]);
  }
  event_storage_unlock(&global_event_storage);
}

JNIEXPORT
void JNICALL SampledObjectAlloc(jvmtiEnv *jvmti_env,
                                JNIEnv* jni_env,
                                jthread thread,
                                jobject object,
                                jclass object_klass,
                                jlong size) {
  add_thread_count(thread, 1, 1);

  if (event_storage_get_compaction_required(&global_event_storage)) {
    event_storage_compact(&global_event_storage, jni_env);
  }

  event_storage_add(&global_event_storage, jni_env, thread, object,
                    object_klass, size);
}

JNIEXPORT
void JNICALL VMObjectAlloc(jvmtiEnv *jvmti_env,
                           JNIEnv* jni_env,
                           jthread thread,
                           jobject object,
                           jclass object_klass,
                           jlong size) {
  event_storage_add(&second_global_event_storage, jni_env, thread, object,
                    object_klass, size);
}

JNIEXPORT
void JNICALL GarbageCollectionFinish(jvmtiEnv *jvmti_env) {
  event_storage_set_compaction_required(&global_event_storage);
}

static int enable_notifications() {
  if (check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_ENABLE, JVMTI_EVENT_GARBAGE_COLLECTION_FINISH, NULL),
                     "Set event notifications")) {
    return 1;
  }

  return check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_ENABLE, JVMTI_EVENT_SAMPLED_OBJECT_ALLOC, NULL),
                     "Set event notifications");
}

static int enable_notifications_for_two_threads(jthread first, jthread second) {
  if (check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_ENABLE, JVMTI_EVENT_GARBAGE_COLLECTION_FINISH, NULL),
                           "Set event notifications")) {
    return 0;
  }

  if (check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_ENABLE, JVMTI_EVENT_SAMPLED_OBJECT_ALLOC, first),
                  "Set event notifications")) {
    return 0;
  }

  // Second thread should fail.
  if (check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_ENABLE, JVMTI_EVENT_SAMPLED_OBJECT_ALLOC, second),
                  "Set event notifications")) {
    return 0;
  }

  return 1;
}

static
jint Agent_Initialize(JavaVM *jvm, char *options, void *reserved) {
  jint res;
  jvmtiEventCallbacks callbacks;
  jvmtiCapabilities caps;

  res = JNI_ENV_PTR(jvm)->GetEnv(JNI_ENV_ARG(jvm, (void **) &jvmti),
                                 JVMTI_VERSION_9);
  if (res != JNI_OK || jvmti == NULL) {
    fprintf(stderr, "Error: wrong result of a valid call to GetEnv!\n");
    return JNI_ERR;
  }

  // Get second jvmti environment.
  res = JNI_ENV_PTR(jvm)->GetEnv(JNI_ENV_ARG(jvm, (void **) &second_jvmti),
                                 JVMTI_VERSION_9);
  if (res != JNI_OK || second_jvmti == NULL) {
    fprintf(stderr, "Error: wrong result of a valid second call to GetEnv!\n");
    return JNI_ERR;
  }

  if (PRINT_OUT) {
    fprintf(stderr, "Storage is at %p, secondary is at %p\n",
            &global_event_storage, &second_global_event_storage);
  }

  (*jvmti)->CreateRawMonitor(jvmti, "storage_monitor",
                             &global_event_storage.storage_monitor);
  (*jvmti)->CreateRawMonitor(jvmti, "second_storage_monitor",
                             &second_global_event_storage.storage_monitor);

  (*jvmti)->CreateRawMonitor(jvmti, "compaction_monitor",
                             &global_event_storage.compaction_monitor);
  (*jvmti)->CreateRawMonitor(jvmti, "second_compaction_monitor",
                             &second_global_event_storage.compaction_monitor);

  event_storage_set_garbage_history(&global_event_storage, 200);
  event_storage_set_garbage_history(&second_global_event_storage, 200);

  memset(&callbacks, 0, sizeof(callbacks));
  callbacks.SampledObjectAlloc = &SampledObjectAlloc;
  callbacks.VMObjectAlloc = &VMObjectAlloc;
  callbacks.GarbageCollectionFinish = &GarbageCollectionFinish;

  memset(&caps, 0, sizeof(caps));
  // Get line numbers, sample events, filename, and gc events for the tests.
  caps.can_get_line_numbers = 1;
  caps.can_get_source_file_name = 1;
  caps.can_generate_garbage_collection_events = 1;
  caps.can_generate_sampled_object_alloc_events = 1;
  caps.can_generate_vm_object_alloc_events = 1;
  if (check_error((*jvmti)->AddCapabilities(jvmti, &caps), "Add capabilities")) {
    return JNI_ERR;
  }

  if (check_error((*jvmti)->SetEventCallbacks(jvmti, &callbacks,
                                              sizeof(jvmtiEventCallbacks)),
                  " Set Event Callbacks")) {
    return JNI_ERR;
  }
  return JNI_OK;
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitor_setSamplingRate(JNIEnv* env, jclass cls, jint value) {
  (*jvmti)->SetHeapSamplingRate(jvmti, value);
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitor_eventStorageIsEmpty(JNIEnv* env, jclass cls) {
  return event_storage_get_count(&global_event_storage) == 0;
}

JNIEXPORT jint JNICALL
Java_MyPackage_HeapMonitor_getEventStorageElementCount(JNIEnv* env, jclass cls) {
  return event_storage_get_count(&global_event_storage);
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitor_enableSamplingEvents(JNIEnv* env, jclass cls) {
  enable_notifications();
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitor_enableSamplingEventsForTwoThreads(JNIEnv* env,
                                                             jclass cls,
                                                             jthread first,
                                                             jthread second) {
  return enable_notifications_for_two_threads(first, second);
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitor_disableSamplingEvents(JNIEnv* env, jclass cls) {
  check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_DISABLE, JVMTI_EVENT_SAMPLED_OBJECT_ALLOC, NULL),
              "Set event notifications");

  check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_DISABLE, JVMTI_EVENT_GARBAGE_COLLECTION_FINISH, NULL),
              "Garbage Collection Finish");
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitor_obtainedEvents(JNIEnv* env, jclass cls, jobjectArray frames) {
  jboolean result;
  jsize size = (*env)->GetArrayLength(env, frames);
  ExpectedContentFrame *native_frames = malloc(size * sizeof(*native_frames));

  if (native_frames == NULL) {
    return 0;
  }

  fill_native_frames(env, frames, native_frames, size);
  result = event_storage_contains(env, &global_event_storage, native_frames, size);

  free(native_frames), native_frames = NULL;
  return result;
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitor_garbageContains(JNIEnv* env, jclass cls, jobjectArray frames) {
  jboolean result;
  jsize size = (*env)->GetArrayLength(env, frames);
  ExpectedContentFrame *native_frames = malloc(size * sizeof(*native_frames));

  if (native_frames == NULL) {
    return 0;
  }

  fill_native_frames(env, frames, native_frames, size);
  result = event_storage_garbage_contains(env, &global_event_storage, native_frames, size);

  free(native_frames), native_frames = NULL;
  return result;
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitor_forceGarbageCollection(JNIEnv* env, jclass cls) {
  check_error((*jvmti)->ForceGarbageCollection(jvmti),
              "Forced Garbage Collection");
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitor_resetEventStorage(JNIEnv* env, jclass cls) {
  event_storage_reset(&global_event_storage);
  event_storage_reset(&second_global_event_storage);
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitorNoCapabilityTest_allSamplingMethodsFail(JNIEnv *env,
                                                                  jclass cls) {
  jvmtiCapabilities caps;
  memset(&caps, 0, sizeof(caps));
  caps.can_generate_sampled_object_alloc_events = 1;
  if (check_error((*jvmti)->RelinquishCapabilities(jvmti, &caps),
                  "Add capabilities\n")){
    return FALSE;
  }

  if (check_capability_error((*jvmti)->SetHeapSamplingRate(jvmti, 1<<19),
                             "Set Heap Sampling Rate")) {
    return FALSE;
  }
  return TRUE;
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitorIllegalArgumentTest_testIllegalArgument(JNIEnv *env,
                                                                  jclass cls) {
  if (check_error((*jvmti)->SetHeapSamplingRate(jvmti, 0),
                  "Sampling rate 0 failed\n")){
    return FALSE;
  }

  if (check_error((*jvmti)->SetHeapSamplingRate(jvmti, 1024),
                  "Sampling rate 1024 failed\n")){
    return FALSE;
  }

  if (!check_error((*jvmti)->SetHeapSamplingRate(jvmti, -1),
                   "Sampling rate -1 passed\n")){
    return FALSE;
  }

  if (!check_error((*jvmti)->SetHeapSamplingRate(jvmti, -1024),
                   "Sampling rate -1024 passed\n")){
    return FALSE;
  }

  return TRUE;
}

JNIEXPORT jdouble JNICALL
Java_MyPackage_HeapMonitorStatRateTest_getAverageRate(JNIEnv *env, jclass cls) {
  return event_storage_get_average_rate(&global_event_storage);
}

typedef struct sThreadsFound {
  jthread* threads;
  int num_threads;
} ThreadsFound;

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitorThreadTest_checkSamples(JNIEnv* env, jclass cls,
                                                  jint num_threads) {

  print_thread_stats();
  // Ensure we got stacks from at least num_threads.
  return thread_stats.number_threads >= num_threads;
}

JNIEXPORT
void JNICALL SampledObjectAlloc2(jvmtiEnv *jvmti_env,
                                 JNIEnv* jni_env,
                                 jthread thread,
                                 jobject object,
                                 jclass object_klass,
                                 jlong size) {
  // Nop for now, two agents are not yet implemented.
  assert(0);
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitorTwoAgentsTest_enablingSamplingInSecondaryAgent(
    JNIEnv* env, jclass cls) {
  // Currently this method should be failing directly at the AddCapability step
  // but the implementation is correct for when multi-agent support is enabled.
  jvmtiCapabilities caps;
  jvmtiEventCallbacks callbacks;

  memset(&caps, 0, sizeof(caps));
  caps.can_generate_sampled_object_alloc_events = 1;
  if (check_error((*second_jvmti)->AddCapabilities(second_jvmti, &caps),
                  "Set the capability for second agent")) {
    return FALSE;
  }

  memset(&callbacks, 0, sizeof(callbacks));
  callbacks.SampledObjectAlloc = &SampledObjectAlloc2;

  if (check_error((*second_jvmti)->SetEventCallbacks(second_jvmti, &callbacks,
                                                     sizeof(jvmtiEventCallbacks)),
                  " Set Event Callbacks for second agent")) {
    return FALSE;
  }

  return TRUE;
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitor_enableVMEvents(JNIEnv* env, jclass cls) {
  check_error((*jvmti)->SetEventNotificationMode(
      jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_OBJECT_ALLOC, NULL),
              "Set vm event notifications");
}

JNIEXPORT jint JNICALL
Java_MyPackage_HeapMonitorVMEventsTest_vmEvents(JNIEnv* env, jclass cls) {
  return event_storage_number_additions(&second_global_event_storage);
}

JNIEXPORT jint JNICALL
Java_MyPackage_HeapMonitor_sampledEvents(JNIEnv* env, jclass cls) {
  return event_storage_number_additions(&global_event_storage);
}

static void allocate_object(JNIEnv* env) {
  // Construct an Object.
  jclass cls = (*env)->FindClass(env, "java/lang/Object");
  jmethodID constructor;

  if (cls == NULL) {
    throw_exception(env, "Cannot find Object class.");
    return;
  }

  constructor = (*env)->GetMethodID(env, cls, "<init>", "()V");

  if (constructor == NULL) {
    throw_exception(env, "Cannot find Object class constructor.");
    return;
  }

  // Call back constructor to allocate a new instance, with an int argument
  (*env)->NewObject(env, cls, constructor);
}

// Ensure we got a callback for the test.
static int did_recursive_callback_test;

JNIEXPORT
void JNICALL RecursiveSampledObjectAlloc(jvmtiEnv *jvmti_env,
                                         JNIEnv* jni_env,
                                         jthread thread,
                                         jobject object,
                                         jclass object_klass,
                                         jlong size) {
  // Basically ensure that if we were to allocate objects, we would not have an
  // infinite recursion here.
  int i;
  for (i = 0; i < 1000; i++) {
    allocate_object(jni_env);
  }

  did_recursive_callback_test = 1;
}

JNIEXPORT jboolean JNICALL
Java_MyPackage_HeapMonitorRecursiveTest_didCallback(JNIEnv* env, jclass cls) {
  return did_recursive_callback_test != 0;
}

JNIEXPORT void JNICALL
Java_MyPackage_HeapMonitorRecursiveTest_setCallbackToCallAllocateSomeMore(JNIEnv* env, jclass cls) {
  jvmtiEventCallbacks callbacks;

  memset(&callbacks, 0, sizeof(callbacks));
  callbacks.SampledObjectAlloc = &RecursiveSampledObjectAlloc;

  if (check_error((*jvmti)->SetEventCallbacks(jvmti, &callbacks,
                                              sizeof(jvmtiEventCallbacks)),
                  " Set Event Callbacks")) {
    throw_exception(env, "Cannot reset the callback.");
    return;
  }
}

#ifdef __cplusplus
}
#endif
