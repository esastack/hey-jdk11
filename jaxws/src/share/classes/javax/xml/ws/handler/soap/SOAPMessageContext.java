/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.xml.ws.handler.soap;

import javax.xml.soap.SOAPMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import java.util.Set;

/** The interface <code>SOAPMessageContext</code>
 *  provides access to the SOAP message for either RPC request or
 *  response. The <code>javax.xml.soap.SOAPMessage</code> specifies
 *  the standard Java API for the representation of a SOAP 1.1 message
 *  with attachments.
 *
 *  @see javax.xml.soap.SOAPMessage
 *
 *  @since JAX-WS 2.0
**/
public interface SOAPMessageContext
                    extends javax.xml.ws.handler.MessageContext {

  /** Gets the SOAPMessage from this message context. Modifications
   *  to the returned SOAPMessage change the message in-place, there
   *  is no need to susequently call <code>setMessage</code>.
   *
   *  @return Returns the SOAPMessage; returns null if no
   *          SOAPMessage is present in this message context
  **/
  public SOAPMessage getMessage();

  /** Sets the SOAPMessage in this message context
   *
   *  @param  message SOAP message
   *  @throws WebServiceException If any error during the setting
   *          of the SOAPMessage in this message context
   *  @throws java.lang.UnsupportedOperationException If this
   *          operation is not supported
  **/
  public void setMessage(SOAPMessage message);

  /** Gets headers that have a particular qualified name from the message in the
   *  message context. Note that a SOAP message can contain multiple headers
   *  with the same qualified name.
   *
   *  @param  header The XML qualified name of the SOAP header(s).
   *  @param  context The JAXBContext that should be used to unmarshall the
   *          header
   *  @param  allRoles If <code>true</code> then returns headers for all SOAP
   *          roles, if <code>false</code> then only returns headers targetted
   *          at the roles currently being played by this SOAP node, see
   *          <code>getRoles</code>.
   *  @return An array of unmarshalled headers; returns an empty array if no
   *          message is present in this message context or no headers match
   *          the supplied qualified name.
   *  @throws WebServiceException If an error occurs when using the supplied
   *     JAXBContext to unmarshall. The cause of
   *     the WebServiceException is the original JAXBException.
  **/
  public Object[] getHeaders(QName header, JAXBContext context,
    boolean allRoles);

  /** Gets the SOAP actor roles associated with an execution
   *  of the handler chain.
   *  Note that SOAP actor roles apply to the SOAP node and
   *  are managed using <code>SOAPBinding.setRoles</code> and
   *  <code>SOAPBinding.getRoles</code>. Handler instances in
   *  the handler chain use this information about the SOAP actor
   *  roles to process the SOAP header blocks. Note that the
   *  SOAP actor roles are invariant during the processing of
   *  SOAP message through the handler chain.
   *
   *  @return Array of String for SOAP actor roles
  **/
  public Set<String> getRoles();
}
