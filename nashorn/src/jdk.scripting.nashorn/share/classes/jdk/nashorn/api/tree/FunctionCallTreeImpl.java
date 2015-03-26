/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

package jdk.nashorn.api.tree;

import java.util.List;
import jdk.nashorn.internal.ir.CallNode;

class FunctionCallTreeImpl extends ExpressionTreeImpl implements FunctionCallTree {
    private final List<? extends ExpressionTree> arguments;
    private final ExpressionTree function;
    FunctionCallTreeImpl(final CallNode node,
            final ExpressionTree function,
            final List<? extends ExpressionTree> arguments) {
        super(node);
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public Kind getKind() {
        return Kind.FUNCTION_INVOCATION;
    }

    @Override
    public ExpressionTree getFunctionSelect() {
        return function;
    }

    @Override
    public List<? extends ExpressionTree> getArguments() {
        return arguments;
    }

    @Override
    public <R,D> R accept(TreeVisitor<R,D> visitor, D data) {
        return visitor.visitFunctionCall(this, data);
    }
}
