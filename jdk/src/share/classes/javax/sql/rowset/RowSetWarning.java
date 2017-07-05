/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.sql.rowset;

import java.sql.SQLException;

/**
 * An extension of <code>SQLException</code> that provides information
 * about database warnings set on <code>RowSet</code> objects.
 * Warnings are silently chained to the object whose method call
 * caused it to be reported.
 * This class complements the <code>SQLWarning</code> class.
 * <P>
 * Rowset warnings may be retrieved from <code>JdbcRowSet</code>,
 * <code>CachedRowSet</code><sup><font size=-2>TM</font></sup>,
 * <code>WebRowSet</code>, <code>FilteredRowSet</code>, or <code>JoinRowSet</code>
 * implementations. To retrieve the first warning reported on any
 * <code>RowSet</code>
 * implementation,  use the method <code>getRowSetWarnings</code> defined
 * in the <code>JdbcRowSet</code> interface or the <code>CachedRowSet</code>
 * interface. To retrieve a warning chained to the first warning, use the
 * <code>RowSetWarning</code> method
 * <code>getNextWarning</code>. To retrieve subsequent warnings, call
 * <code>getNextWarning</code> on each <code>RowSetWarning</code> object that is
 * returned.
 * <P>
 * The inherited methods <code>getMessage</code>, <code>getSQLState</code>,
 * and <code>getErrorCode</code> retrieve information contained in a
 * <code>RowSetWarning</code> object.
 */
public class RowSetWarning extends SQLException {

    /**
     * RowSetWarning object handle.
     */
     private RowSetWarning rwarning;

    /**
     * Constructs a <code>RowSetWarning</code> object
     * with the given value for the reason; SQLState defaults to null,
     * and vendorCode defaults to 0.
     *
     * @param reason a <code>String</code> object giving a description
     *        of the warning; if the <code>String</code> is <code>null</code>,
     *        this constructor behaves like the default (zero parameter)
     *        <code>RowSetWarning</code> constructor
     */
    public RowSetWarning(String reason) {
        super(reason);
    }

    /**
     * Constructs a default <code>RowSetWarning</code> object. The reason
     * defaults to <code>null</code>, SQLState defaults to null and vendorCode
     * defaults to 0.
     */
    public RowSetWarning() {
        super();
    }

    /**
     * Constructs a <code>RowSetWarning</code> object initialized with the
     * given values for the reason and SQLState. The vendor code defaults to 0.
     *
     * If the <code>reason</code> or <code>SQLState</code> parameters are <code>null</code>,
     * this constructor behaves like the default (zero parameter)
     * <code>RowSetWarning</code> constructor.
     *
     * @param reason a <code>String</code> giving a description of the
     *        warning;
     * @param SQLState an XOPEN code identifying the warning; if a non standard
     *        XOPEN <i>SQLState</i> is supplied, no exception is thrown.
     */
    public RowSetWarning(java.lang.String reason, java.lang.String SQLState) {
        super(reason, SQLState);
    }

    /**
     * Constructs a fully specified <code>RowSetWarning</code> object initialized
     * with the given values for the reason, SQLState and vendorCode.
     *
     * If the <code>reason</code>, or the  <code>SQLState</code>
     * parameters are <code>null</code>, this constructor behaves like the default
     * (zero parameter) <code>RowSetWarning</code> constructor.
     *
     * @param reason a <code>String</code> giving a description of the
     *        warning;
     * @param SQLState an XOPEN code identifying the warning; if a non standard
     *        XPOEN <i>SQLState</i> is supplied, no exception is thrown.
     * @param vendorCode a database vendor-specific warning code
     */
    public RowSetWarning(java.lang.String reason, java.lang.String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    /**
     * Retrieves the warning chained to this <code>RowSetWarning</code>
     * object.
     *
     * @return the <code>RowSetWarning</code> object chained to this one; if no
     *         <code>RowSetWarning</code> object is chained to this one,
     *         <code>null</code> is returned (default value)
     * @see #setNextWarning
     */
    public RowSetWarning getNextWarning() {
        return rwarning;
    }

    /**
     * Sets <i>warning</i> as the next warning, that is, the warning chained
     * to this <code>RowSetWarning</code> object.
     *
     * @param warning the <code>RowSetWarning</code> object to be set as the
     *     next warning; if the <code>RowSetWarning</code> is null, this
     *     represents the finish point in the warning chain
     * @see #getNextWarning
     */
    public void setNextWarning(RowSetWarning warning) {
        rwarning = warning;
    }

    static final long serialVersionUID = 6678332766434564774L;
}
