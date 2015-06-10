/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 */

import java.io.FilePermission;
import java.io.IOException;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.Optional;
import java.util.PropertyPermission;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class PermissionTest {
    /**
     * Backing up policy.
     */
    protected static Policy policy;

    /**
     * Backing up security manager.
     */
    private static SecurityManager sm;

    /**
     * Current process handle.
     */
    private final ProcessHandle currentHndl;

    PermissionTest() {
        policy = Policy.getPolicy();
        sm = System.getSecurityManager();
        currentHndl = ProcessHandle.current();
    }

    @Test
    public void allChildrenWithPermission() {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        currentHndl.allChildren();
    }

    @Test
    public void allProcessesWithPermission() {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        ProcessHandle.allProcesses();
    }

    @Test
    public void childrenWithPermission() {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        currentHndl.children();
    }

    @Test
    public void currentWithPermission() {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        ProcessHandle.current();
    }

    @Test
    public void ofWithPermission() {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        ProcessHandle.of(0);
    }

    @Test
    public void parentWithPermission() {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        currentHndl.parent();
    }

    @Test
    public void processToHandleWithPermission() throws IOException {
        Policy.setPolicy(new TestPolicy(new RuntimePermission("manageProcess")));
        Process p = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("sleep", "30");
            p = pb.start();
            ProcessHandle ph = p.toHandle();
            Assert.assertNotNull(ph, "ProcessHandle expected from Process");
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @BeforeGroups (groups = {"NoManageProcessPermission"})
    public void noPermissionsSetup(){
        Policy.setPolicy(new TestPolicy());
        SecurityManager sm = new SecurityManager();
        System.setSecurityManager(sm);
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionAllChildren() {
        currentHndl.allChildren();
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionAllProcesses() {
        ProcessHandle.allProcesses();
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionChildren() {
        currentHndl.children();
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionCurrent() {
        ProcessHandle.current();
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionOf() {
        ProcessHandle.of(0);
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionParent() {
        currentHndl.parent();
    }

    @Test(groups = { "NoManageProcessPermission" }, expectedExceptions = SecurityException.class)
    public void noPermissionProcessToHandle() throws IOException {
        Process p = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("sleep", "30");
            p = pb.start();
            ProcessHandle ph = p.toHandle();
            Assert.assertNotNull(ph, "ProcessHandle expected from Process");
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        System.setSecurityManager(sm);
        Policy.setPolicy(policy);
    }
}

class TestPolicy extends Policy {
    private final PermissionCollection permissions = new Permissions();

    public TestPolicy() {
        setBasicPermissions();
    }

    /*
     * Defines the minimal permissions required by testNG and set security
     * manager permission when running these tests.
     */
    public void setBasicPermissions() {
        permissions.add(new SecurityPermission("getPolicy"));
        permissions.add(new SecurityPermission("setPolicy"));
        permissions.add(new RuntimePermission("getClassLoader"));
        permissions.add(new RuntimePermission("setSecurityManager"));
        permissions.add(new RuntimePermission("createSecurityManager"));
        permissions.add(new PropertyPermission("testng.show.stack.frames",
                "read"));
        permissions.add(new PropertyPermission("user.dir", "read"));
        permissions.add(new PropertyPermission("test.src", "read"));
        permissions.add(new PropertyPermission("file.separator", "read"));
        permissions.add(new PropertyPermission("line.separator", "read"));
        permissions.add(new PropertyPermission("fileStringBuffer", "read"));
        permissions.add(new PropertyPermission("dataproviderthreadcount", "read"));
        permissions.add(new FilePermission("<<ALL FILES>>", "execute"));
    }

    public TestPolicy(Permission... ps) {
        setBasicPermissions();
        Arrays.stream(ps).forEach(p -> permissions.add(p));
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return permissions;
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return permissions;
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission perm) {
        return permissions.implies(perm);
    }
}
