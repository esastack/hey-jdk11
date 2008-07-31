/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * @test
 * @bug 6238731
 * @summary Check that the expected notification is received by the JMX
 *          client even when the domain in the ObjectName is not specified
 * @author Shanliang JIANG
 * @run clean EmptyDomainNotificationTest
 * @run build EmptyDomainNotificationTest
 * @run main EmptyDomainNotificationTest classic
 * @run main EmptyDomainNotificationTest event
 */

import java.util.Collections;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

public class EmptyDomainNotificationTest {

    public static interface SimpleMBean {
        public void emitNotification();
    }

    public static class Simple
        extends NotificationBroadcasterSupport
        implements SimpleMBean {
        public void emitNotification() {
            sendNotification(new Notification("simple", this, 0));
        }
    }

    public static class Listener implements NotificationListener {
        public void handleNotification(Notification n, Object h) {
        System.out.println(
          "EmptyDomainNotificationTest-Listener-handleNotification: receives:" + n);

            if (n.getType().equals("simple")) {
                synchronized(this) {
                    received++;

                    this.notifyAll();
                }
            }
        }

        public int received;
    }

    public static void main(String[] args) throws Exception {

        String type = args[0];
        boolean eventService;
        if (type.equals("classic"))
            eventService = false;
        else if (type.equals("event"))
            eventService = true;
        else
            throw new IllegalArgumentException(type);

        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://");

        Map<String, String> env = Collections.singletonMap(
                RMIConnectorServer.DELEGATE_TO_EVENT_SERVICE,
                Boolean.toString(eventService));

        JMXConnectorServer server =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        server.start();

        JMXConnector client = JMXConnectorFactory.connect(server.getAddress(), null);

        final MBeanServerConnection mbsc = client.getMBeanServerConnection();

        final ObjectName mbean = ObjectName.getInstance(":type=Simple");
        mbsc.createMBean(Simple.class.getName(), mbean);

        System.out.println("EmptyDomainNotificationTest-main: add a listener ...");
        final Listener li = new Listener();
        mbsc.addNotificationListener(mbean, li, null, null);

        System.out.println("EmptyDomainNotificationTest-main: ask to send a notif ...");
        mbsc.invoke(mbean, "emitNotification", null, null);

        System.out.println("EmptyDomainNotificationTest-main: waiting notif...");
        final long stopTime = System.currentTimeMillis() + 2000;
        synchronized(li) {
            long toWait = stopTime - System.currentTimeMillis();

            while (li.received < 1 && toWait > 0) {
                li.wait(toWait);

                toWait = stopTime - System.currentTimeMillis();
            }
        }

        if (li.received < 1) {
            throw new RuntimeException("No notif received!");
        } else if (li.received > 1) {
            throw new RuntimeException("Wait one notif but got: "+li.received);
        }

        System.out.println("EmptyDomainNotificationTest-main: Got the expected notif!");

        System.out.println("EmptyDomainNotificationTest-main: remove the listener.");
        mbsc.removeNotificationListener(mbean, li);

        // clean
        client.close();
        server.stop();

        System.out.println("EmptyDomainNotificationTest-main: Bye.");
    }
}
