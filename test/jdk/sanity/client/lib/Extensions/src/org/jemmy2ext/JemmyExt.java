/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.jemmy2ext;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.DefaultCharBindingMap;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.scrolling.JSpinnerDriver;
import org.netbeans.jemmy.image.StrictImageComparator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.PNGEncoder;
import static org.testng.AssertJUnit.*;

/**
 * This class solves two tasks: 1. It adds functionality that is missing in
 * Jemmy 2. It references all the Jemmy API that is needed by tests so that they
 * can just @build JemmyExt class and do not worry about Jemmy
 *
 * @author akouznet
 */
public class JemmyExt {

    /**
     * Statically referencing all the classes that are needed by tests so that
     * they're compiled by jtreg
     */
    static final Class<?>[] DEPENDENCIES = {
        JSpinnerDriver.class,
        DefaultCharBindingMap.class
    };

    public static void assertNotBlack(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        try {
            assertFalse("All pixels are not black", IntStream.range(0, w).parallel().allMatch(x
                    -> IntStream.range(0, h).allMatch(y -> (image.getRGB(x, y) & 0xffffff) == 0)
            ));
        } catch (Throwable t) {
            save(image, "allPixelsAreBlack.png");
            throw t;
        }
    }

    public static void waitArmed(JButtonOperator button) {
        button.waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return isArmed(button);
            }

            @Override
            public String getDescription() {
                return "Button is armed";
            }
        });
    }

    public static boolean isArmed(JButtonOperator button) {
        return button.getQueueTool().invokeSmoothly(new QueueTool.QueueAction<Boolean>("getModel().isArmed()") {

            @Override
            public Boolean launch() throws Exception {
                return ((JButton) button.getSource()).getModel().isArmed();
            }
        });
    }

    public static void waitPressed(JButtonOperator button) {
        button.waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return isPressed(button);
            }

            @Override
            public String getDescription() {
                return "Button is pressed";
            }
        });
    }

    public static boolean isPressed(JButtonOperator button) {
        return button.getQueueTool().invokeSmoothly(new QueueTool.QueueAction<Boolean>("getModel().isPressed()") {

            @Override
            public Boolean launch() throws Exception {
                return ((JButton) button.getSource()).getModel().isPressed();
            }
        });
    }

    public static void assertEquals(String string, StrictImageComparator comparator, BufferedImage expected, BufferedImage actual) {
        try {
            assertTrue(string, comparator.compare(expected, actual));
        } catch (Error err) {
            save(expected, "expected.png");
            save(actual, "actual.png");
            throw err;
        }
    }

    public static void assertNotEquals(String string, StrictImageComparator comparator, BufferedImage notExpected, BufferedImage actual) {
        try {
            assertFalse(string, comparator.compare(notExpected, actual));
        } catch (Error err) {
            save(notExpected, "notExpected.png");
            save(actual, "actual.png");
            throw err;
        }
    }

    public static void save(BufferedImage image, String filename) {
        String filepath = filename;
        try {
            filepath = new File(filename).getCanonicalPath();
            System.out.println("Saving screenshot to " + filepath);
            BufferedOutputStream file = new BufferedOutputStream(new FileOutputStream(filepath));
            new PNGEncoder(file, PNGEncoder.COLOR_MODE).encode(image);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to save image to " + filepath, ioe);
        }
    }

    public static void waitImageIsStill(Robot rob, ComponentOperator operator) {
        operator.waitState(new ComponentChooser() {

            private BufferedImage previousImage = null;
            private int index = 0;
            private final StrictImageComparator sComparator = new StrictImageComparator();

            @Override
            public boolean checkComponent(Component comp) {
                BufferedImage currentImage = capture(rob, operator);
                save(currentImage, "waitImageIsStill" + index + ".png");
                index++;
                boolean compareResult = previousImage == null ? false : sComparator.compare(currentImage, previousImage);
                previousImage = currentImage;
                return compareResult;
            }

            @Override
            public String getDescription() {
                return "Image of " + operator + " is still";
            }
        });
    }

    private static class ThrowableHolder {

        volatile Throwable t;
    }

    public static void waitFor(String description, RunnableWithException r) throws Exception {
        Waiter<Boolean, ThrowableHolder> waiter = new Waiter<>(new Waitable<Boolean, ThrowableHolder>() {

            @Override
            public Boolean actionProduced(ThrowableHolder obj) {
                try {
                    r.run();
                    return true;
                } catch (Throwable t) {
                    obj.t = t;
                    return null;
                }
            }

            @Override
            public String getDescription() {
                return description;
            }
        });
        ThrowableHolder th = new ThrowableHolder();
        try {
            waiter.waitAction(th);
        } catch (TimeoutExpiredException tee) {
            Throwable t = th.t;
            if (t != null) {
                t.addSuppressed(tee);
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new IllegalStateException("Unexpected exception type", t);
                }
            }
        }
    }

    public static BufferedImage capture(Robot rob, ComponentOperator operator) {
        Rectangle boundary = new Rectangle(operator.getLocationOnScreen(),
                operator.getSize());
        return rob.createScreenCapture(boundary);
    }

    /**
     * Dispose all AWT/Swing windows causing event thread to stop
     */
    public static void disposeAllWindows() {
        System.out.println("disposeAllWindows");
        try {
            EventQueue.invokeAndWait(() -> {
                Window[] windows = Window.getWindows();
                for (Window w : windows) {
                    w.dispose();
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(JemmyExt.class.getName()).log(Level.SEVERE, "Failed to dispose all windows", ex);
        }
    }

    /**
     * This is a helper class which allows to catch throwables thrown in other
     * threads and throw them in the main test thread
     */
    public static class MultiThreadedTryCatch {

        private final List<Throwable> throwables
                = Collections.synchronizedList(new ArrayList<>());

        /**
         * Throws registered throwables. If the list of the registered
         * throwables is not empty, it re-throws the first throwable in the list
         * adding all others into its suppressed list. Can be used in any
         * thread.
         *
         * @throws Exception
         */
        public void throwRegistered() throws Exception {
            Throwable root = null;
            synchronized (throwables) {
                if (!throwables.isEmpty()) {
                    root = throwables.remove(0);
                    while (!throwables.isEmpty()) {
                        root.addSuppressed(throwables.remove(0));
                    }
                }
            }
            if (root != null) {
                if (root instanceof Error) {
                    throw (Error) root;
                } else if (root instanceof Exception) {
                    throw (Exception) root;
                } else {
                    throw new AssertionError("Unexpected exception type: " + root.getClass() + " (" + root + ")");
                }
            }
        }

        /**
         * Registers a throwable and adds it to the list of throwables. Can be
         * used in any thread.
         *
         * @param t
         */
        public void register(Throwable t) {
            t.printStackTrace();
            throwables.add(t);
        }

        /**
         * Registers a throwable and adds it as the first item of the list of
         * catched throwables.
         *
         * @param t
         */
        public void registerRoot(Throwable t) {
            t.printStackTrace();
            throwables.add(0, t);
        }
    }

    /**
     * Trying to capture as much information as possible. Currently it includes
     * full dump and a screenshot of the whole screen.
     */
    public static void captureAll() {
        String lookAndFeelClassName = UIManager.getLookAndFeel().getClass().getSimpleName();
        PNGEncoder.captureScreen("failure_" + lookAndFeelClassName + ".png", PNGEncoder.COLOR_MODE);
        try {
            Dumper.dumpAll("dumpAll_" + lookAndFeelClassName + ".xml");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JemmyExt.class.getName()).log(Level.SEVERE, null, ex);
        }
        captureWindows(lookAndFeelClassName);
    }

    /**
     * Captures each showing window image using Window.paint() method.
     * @param lookAndFeelClassName
     */
    private static void captureWindows(String lookAndFeelClassName) {
        try {
            EventQueue.invokeAndWait(() -> {
                Window[] windows = Window.getWindows();
                int index = 0;
                for (Window w : windows) {
                    if (!w.isShowing()) {
                        continue;
                    }
                    BufferedImage img = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics g = img.getGraphics();
                    w.paint(g);
                    g.dispose();

                    try {
                        ImageIO.write(img, "png", new File("window_" + lookAndFeelClassName
                                + "_" + index++ + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(JemmyExt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static interface RunnableWithException {

        public void run() throws Exception;
    }

    public static void waitIsFocused(JFrameOperator jfo) {
        jfo.waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return jfo.isFocused();
            }

            @Override
            public String getDescription() {
                return "JFrame is focused";
            }
        });
    }

    public static int getJWindowCount() {
        return new QueueTool().invokeAndWait(new QueueTool.QueueAction<Integer>(null) {

            @Override
            public Integer launch() throws Exception {
                Window[] windows = Window.getWindows();
                int windowCount = 0;
                for (Window w : windows) {
                    if (w.getClass().equals(JWindow.class) && ((JWindow)w).isShowing()) {
                        windowCount++;
                    }
                }
                return windowCount;
            }
        });
    }

    public static JWindow getJWindow() {
        return getJWindow(0);
    }

    public static JWindow getJWindow(int index) {
        return new QueueTool().invokeAndWait(new QueueTool.QueueAction<JWindow>(null) {

            @Override
            public JWindow launch() throws Exception {
                Window[] windows = Window.getWindows();
                int windowIndex = 0;
                for (Window w : windows) {
                    if (w.getClass().equals(JWindow.class) && ((JWindow)w).isShowing()) {
                        if (windowIndex == index) {
                            return (JWindow) w;
                        }
                        windowIndex++;
                    }
                }
                return null;
            }
        });
    }

    public static boolean isIconified(FrameOperator frameOperator) {
        return frameOperator.getQueueTool().invokeAndWait(new QueueTool.QueueAction<Boolean>("Frame is iconified") {

            @Override
            public Boolean launch() throws Exception {
                return (((Frame) frameOperator.getSource()).getState() & Frame.ICONIFIED) != 0;
            }
        });
    }

    public static final Operator.DefaultStringComparator EXACT_STRING_COMPARATOR
            = new Operator.DefaultStringComparator(true, true);

    /**
     * Finds a label with the exact labelText and returns the operator for its
     * parent container.
     *
     * @param container
     * @param labelText
     * @return
     */
    public static ContainerOperator<?> getLabeledContainerOperator(ContainerOperator<?> container, String labelText) {

        container.setComparator(EXACT_STRING_COMPARATOR);

        JLabelOperator jLabelOperator = new JLabelOperator(container, labelText);

        assert labelText.equals(jLabelOperator.getText());

        return new ContainerOperator<>(jLabelOperator.getParent());
    }

    /**
     * Finds a JPanel with exact title text.
     *
     * @param container
     * @param titleText
     * @return
     */
    public static ContainerOperator<?> getBorderTitledJPanelOperator(ContainerOperator<?> container, String titleText) {
        return new ContainerOperator<>(container, new JPanelByBorderTitleFinder(titleText, EXACT_STRING_COMPARATOR));
    }

    public static final QueueTool QUEUE_TOOL = new QueueTool();

    /**
     * Allows to find JPanel by the title text in its border.
     */
    public static class JPanelByBorderTitleFinder implements ComponentChooser {

        String titleText;
        Operator.StringComparator comparator;

        /**
         * @param titleText title text pattern
         * @param comparator specifies string comparison algorithm.
         */
        public JPanelByBorderTitleFinder(String titleText, Operator.StringComparator comparator) {
            this.titleText = titleText;
            this.comparator = comparator;
        }

        /**
         * @param titleText title text pattern
         */
        public JPanelByBorderTitleFinder(String titleText) {
            this(titleText, Operator.getDefaultStringComparator());
        }

        @Override
        public boolean checkComponent(Component comp) {
            assert EventQueue.isDispatchThread();
            if (comp instanceof JPanel) {
                return checkBorder(((JPanel) comp).getBorder());
            }
            return false;
        }

        public boolean checkBorder(Border border) {
            if (border instanceof TitledBorder) {
                String title = ((TitledBorder) border).getTitle();
                return comparator.equals(title, titleText);
            } else if (border instanceof CompoundBorder) {
                CompoundBorder compoundBorder = (CompoundBorder) border;
                return checkBorder(compoundBorder.getInsideBorder()) || checkBorder(compoundBorder.getOutsideBorder());
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return ("JPanel with border title text \"" + titleText + "\" with comparator " + comparator);
        }
    }

    public static class ByClassSimpleNameChooser implements ComponentChooser {

        private final String className;

        public ByClassSimpleNameChooser(String className) {
            this.className = className;
        }

        @Override
        public boolean checkComponent(Component comp) {
            return comp.getClass().getSimpleName().equals(className);
        }

        @Override
        public String getDescription() {
            return "Component with the simple class name of " + className;
        }

    }

    public static class ByClassChooser implements ComponentChooser {

        private final Class<?> clazz;

        public ByClassChooser(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean checkComponent(Component comp) {
            return comp.getClass().equals(clazz) && comp.isShowing();
        }

        @Override
        public String getDescription() {
            return "Component with the class of " + clazz;
        }

    }

    public static class ByToolTipChooser implements ComponentChooser {

        private final String tooltip;

        public ByToolTipChooser(String tooltip) {
            if (tooltip == null) {
                throw new NullPointerException("Tooltip cannot be null");
            }
            this.tooltip = tooltip;
        }

        @Override
        public boolean checkComponent(Component comp) {
            return (comp instanceof JComponent)
                    ? tooltip.equals(((JComponent) comp).getToolTipText())
                    : false;
        }

        @Override
        public String getDescription() {
            return "JComponent with the tooltip '" + tooltip + "'";
        }

    }

    @SuppressWarnings(value = "unchecked")
    public static <R, O extends Operator, S extends Component> R getUIValue(O operator, Function<S, R> getter) {
        return operator.getQueueTool().invokeSmoothly(new QueueTool.QueueAction<R>("getting UI value through the queue using " + getter) {

            @Override
            public R launch() throws Exception {
                return getter.apply((S) operator.getSource());
            }
        });
    }
}
