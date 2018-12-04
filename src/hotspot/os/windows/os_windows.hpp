/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef OS_WINDOWS_VM_OS_WINDOWS_HPP
#define OS_WINDOWS_VM_OS_WINDOWS_HPP
// Win32_OS defines the interface to windows operating systems

#define S_ISCHR(mode)   (((mode) & _S_IFCHR) == _S_IFCHR)
#define S_ISFIFO(mode)  (((mode) & _S_IFIFO) == _S_IFIFO)

// strtok_s is the Windows thread-safe equivalent of POSIX strtok_r
#define strtok_r strtok_s

// Information about the protection of the page at address '0' on this os.
static bool zero_page_read_protected() { return true; }

// File conventions
static const char* file_separator() { return "\\"; }
static const char* line_separator() { return "\r\n"; }
static const char* path_separator() { return ";"; }

class win32 {
  friend class os;
  friend unsigned __stdcall thread_native_entry(class Thread*);

 protected:
  static int    _vm_page_size;
  static int    _vm_allocation_granularity;
  static int    _processor_type;
  static int    _processor_level;
  static julong _physical_memory;
  static size_t _default_stack_size;
  static bool   _is_windows_server;
  static bool   _has_exit_bug;

  static void print_windows_version(outputStream* st);

 public:
  // Windows-specific interface:
  static void   initialize_system_info();
  static void   setmode_streams();

  // Processor info as provided by NT
  static int processor_type()  { return _processor_type;  }
  static int processor_level() {
    return _processor_level;
  }
  static julong available_memory();
  static julong physical_memory() { return _physical_memory; }

  // load dll from Windows system directory or Windows directory
  static HINSTANCE load_Windows_dll(const char* name, char *ebuf, int ebuflen);

 private:
  enum Ept { EPT_THREAD, EPT_PROCESS, EPT_PROCESS_DIE };
  // Wrapper around _endthreadex(), exit() and _exit()
  static int exit_process_or_thread(Ept what, int exit_code);

  static void initialize_performance_counter();

 public:
  // Generic interface:

  // Trace number of created threads
  static          intx  _os_thread_limit;
  static volatile intx  _os_thread_count;

  // Tells whether this is a server version of Windows
  static bool is_windows_server() { return _is_windows_server; }

  // Tells whether there can be the race bug during process exit on this platform
  static bool has_exit_bug() { return _has_exit_bug; }

  // Returns the byte size of a virtual memory page
  static int vm_page_size() { return _vm_page_size; }

  // Returns the size in bytes of memory blocks which can be allocated.
  static int vm_allocation_granularity() { return _vm_allocation_granularity; }

  // Read the headers for the executable that started the current process into
  // the structure passed in (see winnt.h).
  static void read_executable_headers(PIMAGE_NT_HEADERS);

  // Default stack size for the current process.
  static size_t default_stack_size() { return _default_stack_size; }

  static bool get_frame_at_stack_banging_point(JavaThread* thread,
                          struct _EXCEPTION_POINTERS* exceptionInfo,
                          address pc, frame* fr);

#ifndef _WIN64
  // A wrapper to install a structured exception handler for fast JNI accesors.
  static address fast_jni_accessor_wrapper(BasicType);
#endif

  // filter function to ignore faults on serializations page
  static LONG WINAPI serialize_fault_filter(struct _EXCEPTION_POINTERS* e);

  // Fast access to current thread
protected:
  static int _thread_ptr_offset;
private:
  static void initialize_thread_ptr_offset();
public:
  static inline void set_thread_ptr_offset(int offset) {
    _thread_ptr_offset = offset;
  }
  static inline int get_thread_ptr_offset() { return _thread_ptr_offset; }
};

static void write_memory_serialize_page_with_handler(JavaThread* thread) {
  // Due to chained nature of SEH handlers we have to be sure
  // that our handler is always last handler before an attempt to write
  // into serialization page - it can fault if we access this page
  // right in the middle of protect/unprotect sequence by remote
  // membar logic.
  // __try/__except are very lightweight operations (only several
  // instructions not affecting control flow directly on x86)
  // so we can use it here, on very time critical path
  __try {
    write_memory_serialize_page(thread);
  } __except (win32::serialize_fault_filter((_EXCEPTION_POINTERS*)_exception_info()))
    {}
}

/*
 * Crash protection for the watcher thread. Wrap the callback
 * with a __try { call() }
 * To be able to use this - don't take locks, don't rely on destructors,
 * don't make OS library calls, don't allocate memory, don't print,
 * don't call code that could leave the heap / memory in an inconsistent state,
 * or anything else where we are not in control if we suddenly jump out.
 */
class ThreadCrashProtection : public StackObj {
public:
  static bool is_crash_protected(Thread* thr) {
    return _crash_protection != NULL && _protected_thread == thr;
  }

  ThreadCrashProtection();
  bool call(os::CrashProtectionCallback& cb);
private:
  static Thread* _protected_thread;
  static ThreadCrashProtection* _crash_protection;
  static volatile intptr_t _crash_mux;
};

class PlatformEvent : public CHeapObj<mtSynchronizer> {
  private:
    double CachePad [4] ;   // increase odds that _Event is sole occupant of cache line
    volatile int _Event ;
    HANDLE _ParkHandle ;

  public:       // TODO-FIXME: make dtor private
    ~PlatformEvent() { guarantee (0, "invariant") ; }

  public:
    PlatformEvent() {
      _Event   = 0 ;
      _ParkHandle = CreateEvent (NULL, false, false, NULL) ;
      guarantee (_ParkHandle != NULL, "invariant") ;
    }

    // Exercise caution using reset() and fired() - they may require MEMBARs
    void reset() { _Event = 0 ; }
    int  fired() { return _Event; }
    void park () ;
    void unpark () ;
    int  park (jlong millis) ;
} ;



class PlatformParker : public CHeapObj<mtSynchronizer> {
  protected:
    HANDLE _ParkEvent ;

  public:
    ~PlatformParker () { guarantee (0, "invariant") ; }
    PlatformParker  () {
      _ParkEvent = CreateEvent (NULL, true, false, NULL) ;
      guarantee (_ParkEvent != NULL, "invariant") ;
    }

} ;

#endif // OS_WINDOWS_VM_OS_WINDOWS_HPP
