/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8042261
 * @summary Checking what attribute is generated by annotation Deprecated
 *          or javadoc deprecated for field, method, class(inner/local), interface.
 * @library /tools/lib /tools/javac/lib ../lib
 * @modules jdk.jdeps/com.sun.tools.classfile
 *          jdk.compiler/com.sun.tools.javac.api
 *          jdk.compiler/com.sun.tools.javac.file
 *          jdk.compiler/com.sun.tools.javac.main
 * @build ToolBox TestBase TestResult InMemoryFileManager
 * @run main DeprecatedTest
 */

import com.sun.tools.classfile.Attribute;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Deprecated_attribute;
import com.sun.tools.classfile.Field;
import com.sun.tools.classfile.InnerClasses_attribute;
import com.sun.tools.classfile.InnerClasses_attribute.Info;
import com.sun.tools.classfile.Method;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Map;

public class DeprecatedTest extends TestResult {

    private static final String[] sources = new String[]{
            "@Deprecated public class deprecated {\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated public void deprecated() {}\n"
            + "@Deprecated public int deprecated;\n"
            + "public void notDeprecated() {}\n"
            + "public int notDeprecated;\n"
            + "public void f() {\n"
            + "    @Deprecated class deprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }\n"
            + "    class notDeprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }}\n"
            + "}",
            "@Deprecated public interface deprecated {\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated void deprecated01();\n"
            + "void notDeprecated01();\n"
            + "@Deprecated default void deprecated02() {}\n"
            + "default void notDeprecated02() {}\n"
            + "@Deprecated int deprecated = 0;\n"
            + "int notDeprecated = 0;\n"
            + "}",
            "@Deprecated public enum deprecated {\n"
            + "@Deprecated deprecated, notDeprecated;\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated public void deprecated() {}\n"
            + "public void notDeprecated() {}\n"
            + "public void f() {\n"
            + "    @Deprecated class deprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }\n"
            + "    class notDeprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }}\n"
            + "}",
            "@Deprecated public @interface deprecated {\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated int deprecated() default 0;\n"
            + "int notDeprecated() default 0;\n"
            + "@Deprecated int deprecated = 0;\n"
            + "int notDeprecated = 0;\n"
            + "}",
            "public class notDeprecated {\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated public void deprecated() {}\n"
            + "@Deprecated public int deprecated;\n"
            + "public void notDeprecated() {}\n"
            + "public int notDeprecated;\n"
            + "public void f() {\n"
            + "    @Deprecated class deprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }\n"
            + "    class notDeprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }}\n"
            + "}",
            "public interface notDeprecated {\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated void deprecated01();\n"
            + "void notDeprecated01();\n"
            + "@Deprecated default void deprecated02() {}\n"
            + "default void notDeprecated02() {}\n"
            + "@Deprecated int deprecated = 0;\n"
            + "int notDeprecated = 0;\n"
            + "}",
            "public enum notDeprecated {\n"
            + "@Deprecated deprecated, notDeprecated;\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated public void deprecated() {}\n"
            + "public void notDeprecated() {}\n"
            + "public void f() {\n"
            + "    @Deprecated class deprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }\n"
            + "    class notDeprecatedLocal {\n"
            + "        @Deprecated int deprecated;\n"
            + "        @Deprecated void deprecated() {}\n"
            + "        int notDeprecated;\n"
            + "        void notDeprecated(){}\n"
            + "    }}\n"
            + "}",
            "public @interface notDeprecated {\n"
            + "@Deprecated class deprecatedInner01 {}\n"
            + "@Deprecated interface deprecatedInner02 {}\n"
            + "@Deprecated enum deprecatedInner03 {}\n"
            + "@Deprecated @interface deprecatedInner04 {}\n"
            + "class notDeprecatedInner01 {}\n"
            + "interface notDeprecatedInner02 {}\n"
            + "enum notDeprecatedInner03 {}\n"
            + "@interface notDeprecatedInner04 {}\n"
            + "@Deprecated int deprecated() default 0;\n"
            + "int notDeprecated() default 0;\n"
            + "@Deprecated int deprecated = 0;\n"
            + "int notDeprecated = 0;\n"
            + "}"};

    public static void main(String[] args) throws TestFailedException {
        new DeprecatedTest().test();
    }

    public void test() throws TestFailedException {
        try {
            for (String src : sources) {
                test(src);
                test(src.replaceAll("@Deprecated", "/** @deprecated */"));
            }
        } catch (Exception e) {
            addFailure(e);
        } finally {
            checkStatus();
        }
    }

    private void test(String src) {
        addTestCase(src);
        printf("Testing test case :\n%s\n", src);
        try {
            Map<String, ? extends JavaFileObject> classes = compile(src).getClasses();
            String outerClassName = classes.containsKey("deprecated")
                    ? "deprecated"
                    : "notDeprecated";
            echo("Testing outer class : " + outerClassName);
            ClassFile cf = readClassFile(classes.get(outerClassName));
            Deprecated_attribute attr = (Deprecated_attribute)
                    cf.getAttribute(Attribute.Deprecated);
            testAttribute(outerClassName, attr, cf);
            testInnerClasses(cf, classes);
            testMethods(cf);
            testFields(cf);
        } catch (Exception e) {
            addFailure(e);
        }
    }

    private void testInnerClasses(ClassFile cf, Map<String, ? extends JavaFileObject> classes)
            throws ConstantPoolException, IOException {
        InnerClasses_attribute innerAttr = (InnerClasses_attribute)
                cf.getAttribute(Attribute.InnerClasses);
        for (Info innerClass : innerAttr.classes) {
            String innerClassName = cf.constant_pool.
                    getClassInfo(innerClass.inner_class_info_index).getName();
            echo("Testing inner class : " + innerClassName);
            ClassFile innerCf = readClassFile(classes.get(innerClassName));
            Deprecated_attribute attr = (Deprecated_attribute)
                    innerCf.getAttribute(Attribute.Deprecated);
            String innerClassSimpleName = innerClass.getInnerName(cf.constant_pool);
            testAttribute(innerClassSimpleName, attr, innerCf);
            if (innerClassName.contains("Local")) {
                testMethods(innerCf);
                testFields(innerCf);
            }
        }
    }

    private void testMethods(ClassFile cf)
            throws ConstantPoolException {
        for (Method m : cf.methods) {
            String methodName = cf.constant_pool.getUTF8Value(m.name_index);
            echo("Testing method : " + methodName);
            Deprecated_attribute attr = (Deprecated_attribute)
                    m.attributes.get(Attribute.Deprecated);
            testAttribute(methodName, attr, cf);
        }
    }

    private void testFields(ClassFile cf) throws ConstantPoolException {
        for (Field f : cf.fields) {
            String fieldName = cf.constant_pool.getUTF8Value(f.name_index);
            echo("Testing field : " + fieldName);
            Deprecated_attribute attr = (Deprecated_attribute)
                    f.attributes.get(Attribute.Deprecated);
            testAttribute(fieldName, attr, cf);
        }
    }

    private void testAttribute(String name, Deprecated_attribute attr, ClassFile cf)
            throws ConstantPoolException {
        if (name.contains("deprecated")) {
            testDeprecatedAttribute(name, attr, cf);
        } else {
            checkNull(attr, name + " should not have deprecated attribute");
        }
    }

    private void testDeprecatedAttribute(String name, Deprecated_attribute attr, ClassFile cf)
            throws ConstantPoolException {
        if (checkNotNull(attr, name + " must have deprecated attribute")) {
            checkEquals(0, attr.attribute_length,
                    "attribute_length should equal to 0");
            checkEquals("Deprecated",
                    cf.constant_pool.getUTF8Value(attr.attribute_name_index),
                    name + " attribute_name_index");
        }
    }
}
