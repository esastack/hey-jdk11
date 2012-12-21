/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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

package jdk.nashorn.internal.runtime;

import static jdk.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk.nashorn.internal.runtime.linker.Lookup.MH;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.dynalang.dynalink.CallSiteDescriptor;
import org.dynalang.dynalink.linker.GuardedInvocation;
import org.dynalang.dynalink.support.CallSiteDescriptorFactory;
import org.dynalang.dynalink.support.Guards;

/**
 * Unique instance of this class is used to represent JavaScript undefined.
 */
public final class Undefined extends DefaultPropertyAccess {

    private Undefined() {
    }

    private static final Undefined UNDEFINED = new Undefined();
    private static final Undefined EMPTY     = new Undefined();

    // Guard used for indexed property access/set on the Undefined instance
    private static final MethodHandle UNDEFINED_GUARD = Guards.getIdentityGuard(UNDEFINED);

    /**
     * Get the value of {@code undefined}, this is represented as a global singleton
     * instance of this class. It can always be reference compared
     *
     * @return the undefined object
     */
    public static Undefined getUndefined() {
        return UNDEFINED;
    }

    /**
     * Get the value of {@code empty}. This is represented as a global singleton
     * instanceof this class. It can always be reference compared.
     * <p>
     * We need empty to differentiate behavior in things like array iterators
     * <p>
     * @return the empty object
     */
    public static Undefined getEmpty() {
        return EMPTY;
    }

    /**
     * Get the class name of Undefined
     * @return "Undefined"
     */
    @SuppressWarnings("static-method")
    public String getClassName() {
        return "Undefined";
    }

    @Override
    public String toString() {
        return "undefined";
    }

    /**
     * Lookup the appropriate method for an invoke dynamic call.
     * @param desc The invoke dynamic callsite descriptor.
     * @return GuardedInvocation to be invoked at call site.
     */
    public static GuardedInvocation lookup(final CallSiteDescriptor desc) {
        final String operator = CallSiteDescriptorFactory.tokenizeOperators(desc).get(0);

        switch (operator) {
        case "new":
        case "call":
            lookupTypeError("cant.call.undefined", desc);
            break;
        case "callMethod":
            lookupTypeError("cant.read.property.of.undefined", desc);
        // NOTE: we support getElem and setItem as JavaScript doesn't distinguish items from properties. Nashorn itself
        // emits "dyn:getProp:identifier" for "<expr>.<identifier>" and "dyn:getElem" for "<expr>[<expr>]", but we are
        // more flexible here and dispatch not on operation name (getProp vs. getElem), but rather on whether the
        // operation has an associated name or not.
            break;
        case "getProp":
        case "getElem":
        case "getMethod":
            if (desc.getNameTokenCount() < 3) {
                return findGetIndexMethod(desc);
            }
            lookupTypeError("cant.read.property.of.undefined", desc);
            break;
        case "setProp":
        case "setElem":
            if (desc.getNameTokenCount() < 3) {
                return findSetIndexMethod(desc);
            }
            lookupTypeError("cant.set.property.of.undefined", desc);
            break;
        default:
            break;
        }

        return null;
    }

    private static void lookupTypeError(final String msg, final CallSiteDescriptor desc) {
        typeError(Context.getGlobal(), msg, desc.getNameTokenCount() > 2 ? desc.getNameToken(2) : null);
    }

    /**
     * Find the appropriate GETINDEX method for an invoke dynamic call.
     * @param desc The invoke dynamic callsite descriptor
     * @param args arguments
     * @return GuardedInvocation to be invoked at call site.
     */
    private static GuardedInvocation findGetIndexMethod(final CallSiteDescriptor desc, final Object... args) {
        final MethodType callType  = desc.getMethodType();
        final Class<?> returnClass = callType.returnType();
        final Class<?> keyClass    = callType.parameterType(1);

        String name = "get";
        if (returnClass.isPrimitive()) {
            //turn e.g. get with a double into getDouble
            final String returnTypeName = returnClass.getName();
            name += Character.toUpperCase(returnTypeName.charAt(0)) + returnTypeName.substring(1, returnTypeName.length());
        }
        MethodHandle methodHandle = findOwnMH(name, returnClass, keyClass);
        methodHandle = MH.asType(methodHandle, methodHandle.type().changeParameterType(0, Object.class));

        return new GuardedInvocation(methodHandle, UNDEFINED_GUARD);
    }

    /**
     * Find the appropriate SETINDEX method for an invoke dynamic call.
     * @param desc The invoke dynamic callsite descriptor
     * @return GuardedInvocation to be invoked at call site.
     */
    private static GuardedInvocation findSetIndexMethod(final CallSiteDescriptor desc) {
        final MethodType callType   = desc.getMethodType();
        final Class<?>   keyClass   = callType.parameterType(1);
        final Class<?>   valueClass = callType.parameterType(2);

        MethodHandle methodHandle = findOwnMH("set", void.class, keyClass, valueClass, boolean.class);
        methodHandle = MH.asType(methodHandle, methodHandle.type().changeParameterType(0, Object.class));
        methodHandle = MH.insertArguments(methodHandle, 3, false);

        return new GuardedInvocation(methodHandle, UNDEFINED_GUARD);
    }

    @Override
    public Object get(final Object key) {
        typeError(Context.getGlobal(), "cant.read.property.of.undefined", ScriptRuntime.safeToString(key));
        return ScriptRuntime.UNDEFINED;
    }

    @Override
    public void set(final Object key, final Object value, final boolean strict) {
        typeError(Context.getGlobal(), "cant.set.property.of.undefined", ScriptRuntime.safeToString(key));
    }

    @Override
    public boolean delete(final Object key, final boolean strict) {
        typeError(Context.getGlobal(), "cant.delete.property.of.undefined", ScriptRuntime.safeToString(key));
        return false;
    }

    @Override
    public boolean has(final Object key) {
        return false;
    }

    @Override
    public boolean hasOwnProperty(final Object key) {
        return false;
    }

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findVirtual(MethodHandles.lookup(), Undefined.class, name, MH.type(rtype, types));
    }
}
