/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
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

// Precompiled headers are turned off for Sun Studion,
// or if the user passes --disable-precompiled-headers to configure.

#ifndef DONT_USE_PRECOMPILED_HEADER
# include "asm/assembler.hpp"
# include "asm/assembler.inline.hpp"
# include "asm/codeBuffer.hpp"
# include "asm/register.hpp"
# include "ci/ciArray.hpp"
# include "ci/ciArrayKlass.hpp"
# include "ci/ciClassList.hpp"
# include "ci/ciConstant.hpp"
# include "ci/ciConstantPoolCache.hpp"
# include "ci/ciEnv.hpp"
# include "ci/ciExceptionHandler.hpp"
# include "ci/ciField.hpp"
# include "ci/ciFlags.hpp"
# include "ci/ciInstance.hpp"
# include "ci/ciInstanceKlass.hpp"
# include "ci/ciKlass.hpp"
# include "ci/ciMethod.hpp"
# include "ci/ciNullObject.hpp"
# include "ci/ciObjArrayKlass.hpp"
# include "ci/ciObject.hpp"
# include "ci/ciObjectFactory.hpp"
# include "ci/ciSignature.hpp"
# include "ci/ciStreams.hpp"
# include "ci/ciSymbol.hpp"
# include "ci/ciType.hpp"
# include "ci/ciTypeArrayKlass.hpp"
# include "ci/ciUtilities.hpp"
# include "ci/compilerInterface.hpp"
# include "classfile/classFileParser.hpp"
# include "classfile/classFileStream.hpp"
# include "classfile/classLoader.hpp"
# include "classfile/javaClasses.hpp"
# include "classfile/moduleEntry.hpp"
# include "classfile/modules.hpp"
# include "classfile/packageEntry.hpp"
# include "classfile/symbolTable.hpp"
# include "classfile/systemDictionary.hpp"
# include "classfile/vmSymbols.hpp"
# include "code/codeBlob.hpp"
# include "code/codeCache.hpp"
# include "code/compressedStream.hpp"
# include "code/debugInfo.hpp"
# include "code/debugInfoRec.hpp"
# include "code/dependencies.hpp"
# include "code/exceptionHandlerTable.hpp"
# include "code/jvmticmlr.h"
# include "code/location.hpp"
# include "code/nativeInst.hpp"
# include "code/nmethod.hpp"
# include "code/oopRecorder.hpp"
# include "code/pcDesc.hpp"
# include "code/relocInfo.hpp"
# include "code/stubs.hpp"
# include "code/vmreg.hpp"
# include "compiler/disassembler.hpp"
# include "compiler/methodLiveness.hpp"
# include "compiler/oopMap.hpp"
# include "gc/cms/allocationStats.hpp"
# include "gc/cms/gSpaceCounters.hpp"
# include "gc/parallel/immutableSpace.hpp"
# include "gc/parallel/mutableSpace.hpp"
# include "gc/parallel/spaceCounters.hpp"
# include "gc/serial/cSpaceCounters.hpp"
# include "gc/serial/defNewGeneration.hpp"
# include "gc/shared/adaptiveSizePolicy.hpp"
# include "gc/shared/ageTable.hpp"
# include "gc/shared/barrierSet.hpp"
# include "gc/shared/blockOffsetTable.hpp"
# include "gc/shared/cardTableModRefBS.hpp"
# include "gc/shared/collectedHeap.hpp"
# include "gc/shared/collectorCounters.hpp"
# include "gc/shared/collectorPolicy.hpp"
# include "gc/shared/gcCause.hpp"
# include "gc/shared/gcLocker.hpp"
# include "gc/shared/gcStats.hpp"
# include "gc/shared/gcUtil.hpp"
# include "gc/shared/genCollectedHeap.hpp"
# include "gc/shared/generation.hpp"
# include "gc/shared/generationCounters.hpp"
# include "gc/shared/modRefBarrierSet.hpp"
# include "gc/shared/referencePolicy.hpp"
# include "gc/shared/referenceProcessor.hpp"
# include "gc/shared/space.hpp"
# include "gc/shared/spaceDecorator.hpp"
# include "gc/shared/taskqueue.hpp"
# include "gc/shared/threadLocalAllocBuffer.hpp"
# include "gc/shared/workgroup.hpp"
# include "interpreter/abstractInterpreter.hpp"
# include "interpreter/bytecode.hpp"
# include "interpreter/bytecodeHistogram.hpp"
# include "interpreter/bytecodeInterpreter.hpp"
# include "interpreter/bytecodeInterpreter.inline.hpp"
# include "interpreter/bytecodeTracer.hpp"
# include "interpreter/bytecodes.hpp"
# include "interpreter/cppInterpreter.hpp"
# include "interpreter/interp_masm.hpp"
# include "interpreter/interpreter.hpp"
# include "interpreter/invocationCounter.hpp"
# include "interpreter/linkResolver.hpp"
# include "interpreter/templateInterpreter.hpp"
# include "interpreter/templateTable.hpp"
# include "jvmtifiles/jvmti.h"
# include "logging/log.hpp"
# include "memory/allocation.hpp"
# include "memory/allocation.inline.hpp"
# include "memory/heap.hpp"
# include "memory/iterator.hpp"
# include "memory/memRegion.hpp"
# include "memory/oopFactory.hpp"
# include "memory/resourceArea.hpp"
# include "memory/universe.hpp"
# include "memory/universe.inline.hpp"
# include "memory/virtualspace.hpp"
# include "oops/arrayKlass.hpp"
# include "oops/arrayOop.hpp"
# include "oops/constMethod.hpp"
# include "oops/instanceKlass.hpp"
# include "oops/instanceOop.hpp"
# include "oops/instanceRefKlass.hpp"
# include "oops/klass.hpp"
# include "oops/klassVtable.hpp"
# include "oops/markOop.hpp"
# include "oops/markOop.inline.hpp"
# include "oops/method.hpp"
# include "oops/methodData.hpp"
# include "oops/objArrayKlass.hpp"
# include "oops/objArrayOop.hpp"
# include "oops/oop.hpp"
# include "oops/oopsHierarchy.hpp"
# include "oops/symbol.hpp"
# include "oops/typeArrayKlass.hpp"
# include "oops/typeArrayOop.hpp"
# include "prims/jni.h"
# include "prims/jvm.h"
# include "prims/jvmtiExport.hpp"
# include "prims/methodHandles.hpp"
# include "runtime/arguments.hpp"
# include "runtime/atomic.hpp"
# include "runtime/deoptimization.hpp"
# include "runtime/extendedPC.hpp"
# include "runtime/fieldDescriptor.hpp"
# include "runtime/fieldType.hpp"
# include "runtime/frame.hpp"
# include "runtime/frame.inline.hpp"
# include "runtime/globals.hpp"
# include "runtime/globals_extension.hpp"
# include "runtime/handles.hpp"
# include "runtime/handles.inline.hpp"
# include "runtime/icache.hpp"
# include "runtime/init.hpp"
# include "runtime/interfaceSupport.hpp"
# include "runtime/java.hpp"
# include "runtime/javaCalls.hpp"
# include "runtime/javaFrameAnchor.hpp"
# include "runtime/jniHandles.hpp"
# include "runtime/monitorChunk.hpp"
# include "runtime/mutex.hpp"
# include "runtime/mutexLocker.hpp"
# include "runtime/objectMonitor.hpp"
# include "runtime/orderAccess.hpp"
# include "runtime/orderAccess.inline.hpp"
# include "runtime/os.hpp"
# include "runtime/osThread.hpp"
# include "runtime/perfData.hpp"
# include "runtime/perfMemory.hpp"
# include "runtime/prefetch.hpp"
# include "runtime/prefetch.inline.hpp"
# include "runtime/reflection.hpp"
# include "runtime/reflectionUtils.hpp"
# include "runtime/registerMap.hpp"
# include "runtime/safepoint.hpp"
# include "runtime/sharedRuntime.hpp"
# include "runtime/signature.hpp"
# include "runtime/stackValue.hpp"
# include "runtime/stackValueCollection.hpp"
# include "runtime/stubCodeGenerator.hpp"
# include "runtime/stubRoutines.hpp"
# include "runtime/synchronizer.hpp"
# include "runtime/thread.hpp"
# include "runtime/timer.hpp"
# include "runtime/unhandledOops.hpp"
# include "runtime/vframe.hpp"
# include "runtime/vmThread.hpp"
# include "runtime/vm_operations.hpp"
# include "runtime/vm_version.hpp"
# include "services/allocationSite.hpp"
# include "services/lowMemoryDetector.hpp"
# include "services/mallocTracker.hpp"
# include "services/memBaseline.hpp"
# include "services/memReporter.hpp"
# include "services/memTracker.hpp"
# include "services/memoryPool.hpp"
# include "services/memoryService.hpp"
# include "services/memoryUsage.hpp"
# include "services/nmtCommon.hpp"
# include "services/virtualMemoryTracker.hpp"
# include "utilities/accessFlags.hpp"
# include "utilities/array.hpp"
# include "utilities/bitMap.hpp"
# include "utilities/bitMap.inline.hpp"
# include "utilities/bytes.hpp"
# include "utilities/constantTag.hpp"
# include "utilities/copy.hpp"
# include "utilities/debug.hpp"
# include "utilities/exceptions.hpp"
# include "utilities/globalDefinitions.hpp"
# include "utilities/growableArray.hpp"
# include "utilities/hashtable.hpp"
# include "utilities/histogram.hpp"
# include "utilities/macros.hpp"
# include "utilities/nativeCallStack.hpp"
# include "utilities/numberSeq.hpp"
# include "utilities/ostream.hpp"
# include "utilities/preserveException.hpp"
# include "utilities/sizes.hpp"
# include "utilities/utf8.hpp"
#ifdef COMPILER2
# include "libadt/dict.hpp"
# include "libadt/set.hpp"
# include "libadt/vectset.hpp"
# include "opto/ad.hpp"
# include "opto/addnode.hpp"
# include "opto/adlcVMDeps.hpp"
# include "opto/block.hpp"
# include "opto/c2_globals.hpp"
# include "opto/callnode.hpp"
# include "opto/castnode.hpp"
# include "opto/cfgnode.hpp"
# include "opto/compile.hpp"
# include "opto/connode.hpp"
# include "opto/convertnode.hpp"
# include "opto/countbitsnode.hpp"
# include "opto/idealGraphPrinter.hpp"
# include "opto/intrinsicnode.hpp"
# include "opto/loopnode.hpp"
# include "opto/machnode.hpp"
# include "opto/matcher.hpp"
# include "opto/memnode.hpp"
# include "opto/movenode.hpp"
# include "opto/mulnode.hpp"
# include "opto/multnode.hpp"
# include "opto/narrowptrnode.hpp"
# include "opto/opaquenode.hpp"
# include "opto/opcodes.hpp"
# include "opto/optoreg.hpp"
# include "opto/phase.hpp"
# include "opto/phaseX.hpp"
# include "opto/regalloc.hpp"
# include "opto/regmask.hpp"
# include "opto/runtime.hpp"
# include "opto/subnode.hpp"
# include "opto/type.hpp"
# include "opto/vectornode.hpp"
#endif // COMPILER2
#ifdef COMPILER1
# include "c1/c1_Compilation.hpp"
# include "c1/c1_Defs.hpp"
# include "c1/c1_FrameMap.hpp"
# include "c1/c1_LIR.hpp"
# include "c1/c1_MacroAssembler.hpp"
# include "c1/c1_ValueType.hpp"
# include "c1/c1_globals.hpp"
#endif // COMPILER1
#if INCLUDE_JVMCI
# include "jvmci/jvmci_globals.hpp"
#endif // INCLUDE_JVMCI
#if INCLUDE_ALL_GCS
# include "gc/cms/compactibleFreeListSpace.hpp"
# include "gc/cms/concurrentMarkSweepGeneration.hpp"
# include "gc/cms/freeChunk.hpp"
# include "gc/cms/parOopClosures.hpp"
# include "gc/cms/promotionInfo.hpp"
# include "gc/cms/yieldingWorkgroup.hpp"
# include "gc/g1/dirtyCardQueue.hpp"
# include "gc/g1/g1BlockOffsetTable.hpp"
# include "gc/g1/g1OopClosures.hpp"
# include "gc/g1/g1_globals.hpp"
# include "gc/g1/ptrQueue.hpp"
# include "gc/g1/satbMarkQueue.hpp"
# include "gc/parallel/gcAdaptivePolicyCounters.hpp"
# include "gc/parallel/objectStartArray.hpp"
# include "gc/parallel/parMarkBitMap.hpp"
# include "gc/parallel/parallelScavengeHeap.hpp"
# include "gc/parallel/psAdaptiveSizePolicy.hpp"
# include "gc/parallel/psCompactionManager.hpp"
# include "gc/parallel/psGCAdaptivePolicyCounters.hpp"
# include "gc/parallel/psGenerationCounters.hpp"
# include "gc/parallel/psOldGen.hpp"
# include "gc/parallel/psVirtualspace.hpp"
# include "gc/parallel/psYoungGen.hpp"
# include "gc/shared/gcPolicyCounters.hpp"
# include "gc/shared/plab.hpp"
#endif // INCLUDE_ALL_GCS

#endif // !DONT_USE_PRECOMPILED_HEADER
