/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.source.tree;

import java.util.List;

/**
 * A tree node for a basic {@code for} loop statement.
 *
 * For example:
 * <pre>
 *   for ( <em>initializer</em> ; <em>condition</em> ; <em>update</em> )
 *       <em>statement</em>
 * </pre>
 *
 * @jls section 14.14.1
 *
 * @author Peter von der Ah&eacute;
 * @author Jonathan Gibbons
 * @since 1.6
 */
public interface ForLoopTree extends StatementTree {
    /**
     * Returns any initializers of the {@code for} statement.
     * The result will be an empty list if there are
     * no initializers
     * @return the initializers
     */
    List<? extends StatementTree> getInitializer();

    /**
     * Returns the condition of the {@code for} statement.
     * May be {@code null} if there is no condition.
     * @return the condition
     */
    ExpressionTree getCondition();

    /**
     * Returns any update expressions of the {@code for} statement.
     * @return the update expressions
     */
    List<? extends ExpressionStatementTree> getUpdate();

    /**
     * Returns the body of the {@code for} statement.
     * @return the body
     */
    StatementTree getStatement();
}
