/*
 * Copyright (c) 2003, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package sun.management;

import java.lang.management.*;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import jdk.internal.misc.JavaNioAccess;
import jdk.internal.misc.SharedSecrets;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;

/**
 * ManagementFactoryHelper provides static factory methods to create
 * instances of the management interface.
 */
public class ManagementFactoryHelper {
    static {
        // make sure that the management lib is loaded within
        // java.lang.management.ManagementFactory
        jdk.internal.misc.Unsafe.getUnsafe().ensureClassInitialized(ManagementFactory.class);
    }

    private static final VMManagement jvm = new VMManagementImpl();

    private ManagementFactoryHelper() {};

    public static VMManagement getVMManagement() {
        return jvm;
    }

    private static ClassLoadingImpl    classMBean = null;
    private static MemoryImpl          memoryMBean = null;
    private static ThreadImpl          threadMBean = null;
    private static RuntimeImpl         runtimeMBean = null;
    private static CompilationImpl     compileMBean = null;
    private static BaseOperatingSystemImpl osMBean = null;

    public static synchronized ClassLoadingMXBean getClassLoadingMXBean() {
        if (classMBean == null) {
            classMBean = new ClassLoadingImpl(jvm);
        }
        return classMBean;
    }

    public static synchronized MemoryMXBean getMemoryMXBean() {
        if (memoryMBean == null) {
            memoryMBean = new MemoryImpl(jvm);
        }
        return memoryMBean;
    }

    public static synchronized ThreadMXBean getThreadMXBean() {
        if (threadMBean == null) {
            threadMBean = new ThreadImpl(jvm);
        }
        return threadMBean;
    }

    public static synchronized RuntimeMXBean getRuntimeMXBean() {
        if (runtimeMBean == null) {
            runtimeMBean = new RuntimeImpl(jvm);
        }
        return runtimeMBean;
    }

    public static synchronized CompilationMXBean getCompilationMXBean() {
        if (compileMBean == null && jvm.getCompilerName() != null) {
            compileMBean = new CompilationImpl(jvm);
        }
        return compileMBean;
    }

    public static synchronized OperatingSystemMXBean getOperatingSystemMXBean() {
        if (osMBean == null) {
            osMBean = new BaseOperatingSystemImpl(jvm);
        }
        return osMBean;
    }

    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        MemoryPoolMXBean[] pools = MemoryImpl.getMemoryPools();
        List<MemoryPoolMXBean> list = new ArrayList<>(pools.length);
        for (MemoryPoolMXBean p : pools) {
            list.add(p);
        }
        return list;
    }

    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        MemoryManagerMXBean[]  mgrs = MemoryImpl.getMemoryManagers();
        List<MemoryManagerMXBean> result = new ArrayList<>(mgrs.length);
        for (MemoryManagerMXBean m : mgrs) {
            result.add(m);
        }
        return result;
    }

     public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        MemoryManagerMXBean[]  mgrs = MemoryImpl.getMemoryManagers();
        List<GarbageCollectorMXBean> result = new ArrayList<>(mgrs.length);
        for (MemoryManagerMXBean m : mgrs) {
            if (GarbageCollectorMXBean.class.isInstance(m)) {
                 result.add(GarbageCollectorMXBean.class.cast(m));
            }
        }
        return result;
    }

    public static PlatformLoggingMXBean getPlatformLoggingMXBean() {
        if (LoggingMXBeanSupport.isAvailable()) {
            return PlatformLoggingImpl.instance;
        } else {
            return null;
        }
    }

    public static boolean isPlatformLoggingMXBeanAvailable() {
        return LoggingMXBeanSupport.isAvailable();
    }

    /**
     * The logging MXBean object is an instance of
     * PlatformLoggingMXBean and java.util.logging.LoggingMXBean
     * but it can't directly implement two MXBean interfaces
     * as a compliant MXBean implements exactly one MXBean interface,
     * or if it implements one interface that is a subinterface of
     * all the others; otherwise, it is a non-compliant MXBean
     * and MBeanServer will throw NotCompliantMBeanException.
     * See the Definition of an MXBean section in javax.management.MXBean spec.
     *
     * To create a compliant logging MXBean, define a LoggingMXBean interface
     * that extend PlatformLoggingMXBean and j.u.l.LoggingMXBean
    */
    public interface LoggingMXBean
        extends PlatformLoggingMXBean, java.util.logging.LoggingMXBean {
    }

    // This is a trick: if java.util.logging is not present then
    // attempting to access something that implements
    // java.util.logging.LoggingMXBean will trigger a CNFE.
    // So we cannot directly call any static method or access any static field
    // on PlatformLoggingImpl, as we would risk raising a CNFE.
    // Instead we use this intermediate LoggingMXBeanSupport class to determine
    // whether java.util.logging is present, and load the actual LoggingMXBean
    // implementation.
    //
    static final class LoggingMXBeanSupport {
        final static Object loggingImpl =
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    // create a LoggingProxyImpl instance when
                    // java.util.logging classes exist
                    Class<?> c = Class.forName("java.util.logging.Logging", true, null);
                    Constructor<?> cons = c.getDeclaredConstructor();
                    cons.setAccessible(true);
                    return cons.newInstance();
                } catch (ClassNotFoundException cnf) {
                    return null;
                } catch (NoSuchMethodException | InstantiationException
                        | IllegalAccessException | InvocationTargetException e) {
                    throw new AssertionError(e);
                }
            }});

        static boolean isAvailable() {
            return loggingImpl != null;
        }
    }

    static class PlatformLoggingImpl implements LoggingMXBean
    {
        final static java.util.logging.LoggingMXBean impl =
                (java.util.logging.LoggingMXBean) LoggingMXBeanSupport.loggingImpl;
        final static PlatformLoggingMXBean instance = new PlatformLoggingImpl();
        final static String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";

        private volatile ObjectName objname;  // created lazily
        @Override
        public ObjectName getObjectName() {
            ObjectName result = objname;
            if (result == null) {
                synchronized (this) {
                    result = objname;
                    if (result == null) {
                        result = Util.newObjectName(LOGGING_MXBEAN_NAME);
                        objname = result;
                    }
                }
            }
            return result;
        }

        @Override
        public java.util.List<String> getLoggerNames() {
            return impl.getLoggerNames();
        }

        @Override
        public String getLoggerLevel(String loggerName) {
            return impl.getLoggerLevel(loggerName);
        }

        @Override
        public void setLoggerLevel(String loggerName, String levelName) {
            impl.setLoggerLevel(loggerName, levelName);
        }

        @Override
        public String getParentLoggerName(String loggerName) {
            return impl.getParentLoggerName(loggerName);
        }
    }

    private static List<BufferPoolMXBean> bufferPools = null;
    public static synchronized List<BufferPoolMXBean> getBufferPoolMXBeans() {
        if (bufferPools == null) {
            bufferPools = new ArrayList<>(2);
            bufferPools.add(createBufferPoolMXBean(SharedSecrets.getJavaNioAccess()
                .getDirectBufferPool()));
            bufferPools.add(createBufferPoolMXBean(sun.nio.ch.FileChannelImpl
                .getMappedBufferPool()));
        }
        return bufferPools;
    }

    private final static String BUFFER_POOL_MXBEAN_NAME = "java.nio:type=BufferPool";

    /**
     * Creates management interface for the given buffer pool.
     */
    private static BufferPoolMXBean
        createBufferPoolMXBean(final JavaNioAccess.BufferPool pool)
    {
        return new BufferPoolMXBean() {
            private volatile ObjectName objname;  // created lazily
            @Override
            public ObjectName getObjectName() {
                ObjectName result = objname;
                if (result == null) {
                    synchronized (this) {
                        result = objname;
                        if (result == null) {
                            result = Util.newObjectName(BUFFER_POOL_MXBEAN_NAME +
                                ",name=" + pool.getName());
                            objname = result;
                        }
                    }
                }
                return result;
            }
            @Override
            public String getName() {
                return pool.getName();
            }
            @Override
            public long getCount() {
                return pool.getCount();
            }
            @Override
            public long getTotalCapacity() {
                return pool.getTotalCapacity();
            }
            @Override
            public long getMemoryUsed() {
                return pool.getMemoryUsed();
            }
        };
    }

    private static HotspotRuntime hsRuntimeMBean = null;
    private static HotspotClassLoading hsClassMBean = null;
    private static HotspotThread hsThreadMBean = null;
    private static HotspotCompilation hsCompileMBean = null;
    private static HotspotMemory hsMemoryMBean = null;

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotRuntimeMBean getHotspotRuntimeMBean() {
        if (hsRuntimeMBean == null) {
            hsRuntimeMBean = new HotspotRuntime(jvm);
        }
        return hsRuntimeMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotClassLoadingMBean getHotspotClassLoadingMBean() {
        if (hsClassMBean == null) {
            hsClassMBean = new HotspotClassLoading(jvm);
        }
        return hsClassMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotThreadMBean getHotspotThreadMBean() {
        if (hsThreadMBean == null) {
            hsThreadMBean = new HotspotThread(jvm);
        }
        return hsThreadMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotMemoryMBean getHotspotMemoryMBean() {
        if (hsMemoryMBean == null) {
            hsMemoryMBean = new HotspotMemory(jvm);
        }
        return hsMemoryMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotCompilationMBean getHotspotCompilationMBean() {
        if (hsCompileMBean == null) {
            hsCompileMBean = new HotspotCompilation(jvm);
        }
        return hsCompileMBean;
    }

    /**
     * Registers a given MBean if not registered in the MBeanServer;
     * otherwise, just return.
     */
    private static void addMBean(MBeanServer mbs, Object mbean, String mbeanName) {
        try {
            final ObjectName objName = Util.newObjectName(mbeanName);

            // inner class requires these fields to be final
            final MBeanServer mbs0 = mbs;
            final Object mbean0 = mbean;
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws MBeanRegistrationException,
                                         NotCompliantMBeanException {
                    try {
                        mbs0.registerMBean(mbean0, objName);
                        return null;
                    } catch (InstanceAlreadyExistsException e) {
                        // if an instance with the object name exists in
                        // the MBeanServer ignore the exception
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw Util.newException(e.getException());
        }
    }

    private final static String HOTSPOT_CLASS_LOADING_MBEAN_NAME =
        "sun.management:type=HotspotClassLoading";

    private final static String HOTSPOT_COMPILATION_MBEAN_NAME =
        "sun.management:type=HotspotCompilation";

    private final static String HOTSPOT_MEMORY_MBEAN_NAME =
        "sun.management:type=HotspotMemory";

    private static final String HOTSPOT_RUNTIME_MBEAN_NAME =
        "sun.management:type=HotspotRuntime";

    private final static String HOTSPOT_THREAD_MBEAN_NAME =
        "sun.management:type=HotspotThreading";

    static void registerInternalMBeans(MBeanServer mbs) {
        // register all internal MBeans if not registered
        // No exception is thrown if a MBean with that object name
        // already registered
        addMBean(mbs, getHotspotClassLoadingMBean(),
            HOTSPOT_CLASS_LOADING_MBEAN_NAME);
        addMBean(mbs, getHotspotMemoryMBean(),
            HOTSPOT_MEMORY_MBEAN_NAME);
        addMBean(mbs, getHotspotRuntimeMBean(),
            HOTSPOT_RUNTIME_MBEAN_NAME);
        addMBean(mbs, getHotspotThreadMBean(),
            HOTSPOT_THREAD_MBEAN_NAME);

        // CompilationMBean may not exist
        if (getCompilationMXBean() != null) {
            addMBean(mbs, getHotspotCompilationMBean(),
                HOTSPOT_COMPILATION_MBEAN_NAME);
        }
    }

    private static void unregisterMBean(MBeanServer mbs, String mbeanName) {
        try {
            final ObjectName objName = Util.newObjectName(mbeanName);

            // inner class requires these fields to be final
            final MBeanServer mbs0 = mbs;
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws MBeanRegistrationException,
                                           RuntimeOperationsException  {
                    try {
                        mbs0.unregisterMBean(objName);
                    } catch (InstanceNotFoundException e) {
                        // ignore exception if not found
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw Util.newException(e.getException());
        }
    }

    static void unregisterInternalMBeans(MBeanServer mbs) {
        // unregister all internal MBeans
        unregisterMBean(mbs, HOTSPOT_CLASS_LOADING_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_MEMORY_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_RUNTIME_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_THREAD_MBEAN_NAME);

        // CompilationMBean may not exist
        if (getCompilationMXBean() != null) {
            unregisterMBean(mbs, HOTSPOT_COMPILATION_MBEAN_NAME);
        }
    }

    public static boolean isThreadSuspended(int state) {
        return ((state & JMM_THREAD_STATE_FLAG_SUSPENDED) != 0);
    }

    public static boolean isThreadRunningNative(int state) {
        return ((state & JMM_THREAD_STATE_FLAG_NATIVE) != 0);
    }

    public static Thread.State toThreadState(int state) {
        // suspended and native bits may be set in state
        int threadStatus = state & ~JMM_THREAD_STATE_FLAG_MASK;
        return jdk.internal.misc.VM.toThreadState(threadStatus);
    }

    // These values are defined in jmm.h
    private static final int JMM_THREAD_STATE_FLAG_MASK = 0xFFF00000;
    private static final int JMM_THREAD_STATE_FLAG_SUSPENDED = 0x00100000;
    private static final int JMM_THREAD_STATE_FLAG_NATIVE = 0x00400000;

    // Invoked by the VM
    private static MemoryPoolMXBean createMemoryPool
        (String name, boolean isHeap, long uThreshold, long gcThreshold) {
        return new MemoryPoolImpl(name, isHeap, uThreshold, gcThreshold);
    }

    private static MemoryManagerMXBean createMemoryManager(String name) {
        return new MemoryManagerImpl(name);
    }

    private static GarbageCollectorMXBean
        createGarbageCollector(String name, String type) {

        // ignore type parameter which is for future extension
        return new GarbageCollectorImpl(name);
    }
}
