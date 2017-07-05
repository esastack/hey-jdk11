/*
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

/*
 * Copyright (c) 2003 by BEA Systems, Inc. All Rights Reserved.
 */

package javax.xml.stream.events;

import java.util.List;

/**
 * This is the top level interface for events dealing with DTDs
 *
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @since 1.6
 */
public interface DTD extends XMLEvent {

  /**
   * Returns the entire Document Type Declaration as a string, including
   * the internal DTD subset.
   * This may be null if there is not an internal subset.
   * If it is not null it must return the entire
   * Document Type Declaration which matches the doctypedecl
   * production in the XML 1.0 specification
   */
  String getDocumentTypeDeclaration();

  /**
   * Returns an implementation defined representation of the DTD.
   * This method may return null if no representation is available.
   */
  Object getProcessedDTD();

  /**
   * Return a List containing the notations declared in the DTD.
   * This list must contain NotationDeclaration events.
   * @see NotationDeclaration
   * @return an unordered list of NotationDeclaration events
   */
  List getNotations();

  /**
   * Return a List containing the general entities,
   * both external and internal, declared in the DTD.
   * This list must contain EntityDeclaration events.
   * @see EntityDeclaration
   * @return an unordered list of EntityDeclaration events
   */
  List getEntities();
}
