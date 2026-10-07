package customskinloader.test.installer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class Main {
    private static final String SIMPLE_INSTALLER_CLASS = "net.minecraftforge.installer.SimpleInstaller";
    private static final String INSTALLER_PANEL_CLASS = "net.minecraftforge.installer.InstallerPanel";
    private static final String INSTALL_DIALOG_TITLE = "Mod system installer";
    private static final String INSTALL_CLIENT_OPTION = "--installClient";
    private static final String CLIENT_ACTION = "CLIENT";

    public static void main(String[] args) throws Exception {
        Class<?> simpleInstallerClass = Class.forName(SIMPLE_INSTALLER_CLASS);
        Method simpleInstallerMain = simpleInstallerClass.getMethod("main", String[].class);
        String specificationVersion = simpleInstallerClass.getPackage().getSpecificationVersion();
        if (specificationVersion == null || !specificationVersion.startsWith("1.")) {
            simpleInstallerMain.invoke(null, (Object) args);
            return;
        }

        File targetDir = null;
        for (int i = 0; i < args.length; i++) {
            if (INSTALL_CLIENT_OPTION.equals(args[i]) && i + 1 < args.length) {
                targetDir = new File(args[i + 1]);
                break;
            }
        }
        int exitCode;
        try {
            exitCode = driveLegacyInstaller(simpleInstallerMain, targetDir);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
            exitCode = 1;
        }
        System.exit(exitCode);
    }

    private static int driveLegacyInstaller(Method simpleInstallerMain, File targetDir) throws Exception {
        final File installDir = targetDir.getCanonicalFile();
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        final AtomicBoolean completed = new AtomicBoolean(false);

        Thread watchdog = new Thread(new Runnable() {
            @Override
            public void run() {
                watchDialogs(completed);
            }
        }, "installer-dialog-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();

        Thread installerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    simpleInstallerMain.invoke(null, (Object) new String[0]);
                } catch (InvocationTargetException e) {
                    failure.set(e.getTargetException() == null ? e : e.getTargetException());
                } catch (Throwable t) {
                    failure.set(t);
                }
            }
        }, "installer-main");
        installerThread.start();

        JDialog installDialog = waitForInstallDialog(installerThread, 300000L);
        if (installDialog != null) {
            driveInstallerPanel(installDialog, installDir);
        }

        installerThread.join();

        Throwable throwable = failure.get();
        if (throwable != null) {
            throwable.printStackTrace(System.err);
            return 1;
        }
        return completed.get() ? 0 : 1;
    }

    private static JDialog waitForInstallDialog(Thread installerThread, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (installerThread.isAlive() && System.currentTimeMillis() < deadline) {
            JDialog dialog = findDialog(INSTALL_DIALOG_TITLE);
            if (dialog != null) {
                return dialog;
            }
            Thread.sleep(50L);
        }
        return findDialog(INSTALL_DIALOG_TITLE);
    }

    private static JDialog findDialog(String title) {
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog && window.isVisible() && title.equals(((JDialog) window).getTitle())) {
                return (JDialog) window;
            }
        }
        return null;
    }

    private static void driveInstallerPanel(JDialog installDialog, File installDir) throws Exception {
        JOptionPane optionPane = null;
        long deadline = System.currentTimeMillis() + 10000L;
        while (optionPane == null && System.currentTimeMillis() < deadline) {
            optionPane = findOptionPane(installDialog);
            if (optionPane == null) {
                Thread.sleep(50L);
            }
        }
        if (optionPane == null) {
            throw new IllegalStateException("JOptionPane not found in installer dialog");
        }
        final Object installerPanel = optionPane.getMessage();
        if (installerPanel == null || !INSTALLER_PANEL_CLASS.equals(installerPanel.getClass().getName())) {
            throw new IllegalStateException("InstallerPanel not found in installer dialog");
        }

        final Class<?> panelClass = installerPanel.getClass();
        final Field targetDirField = panelClass.getDeclaredField("targetDir");
        targetDirField.setAccessible(true);
        final Field selectedDirTextField = panelClass.getDeclaredField("selectedDirText");
        selectedDirTextField.setAccessible(true);
        final Field choiceButtonGroupField = panelClass.getDeclaredField("choiceButtonGroup");
        choiceButtonGroupField.setAccessible(true);
        final Field optionalsField = panelClass.getDeclaredField("optionals");
        optionalsField.setAccessible(true);
        final Method updateFilePathMethod = panelClass.getDeclaredMethod("updateFilePath");
        updateFilePathMethod.setAccessible(true);

        runOnEventThread(new Runnable() {
            @Override
            public void run() {
                try {
                    targetDirField.set(installerPanel, installDir);
                    JTextField selectedDirText = (JTextField) selectedDirTextField.get(installerPanel);
                    selectedDirText.setText(installDir.getPath());
                    ButtonGroup choiceButtonGroup = (ButtonGroup) choiceButtonGroupField.get(installerPanel);
                    Enumeration<AbstractButton> buttons = choiceButtonGroup.getElements();
                    while (buttons.hasMoreElements()) {
                        AbstractButton button = buttons.nextElement();
                        if (CLIENT_ACTION.equals(button.getActionCommand()) && !button.isSelected()) {
                            button.doClick();
                        }
                    }
                    if (choiceButtonGroup.getSelection() == null || !CLIENT_ACTION.equals(choiceButtonGroup.getSelection().getActionCommand())) {
                        throw new IllegalStateException("CLIENT action is not selectable");
                    }

                    // The legacy Forge installer (spec 1.7.7) appends optional libraries via text concatenation;
                    // its "name" line has a Java operator-precedence bug that writes a stray comma, corrupting the JSON
                    // (e.g. Mercurius on 1.11/1.12/1.12.1). Here, all optionals are unchecked before Install is clicked.
                    Object optionals = optionalsField.get(installerPanel);
                    if (optionals != null && optionals.getClass().isArray()) {
                        int optionalsCount = Array.getLength(optionals);
                        for (int i = 0; i < optionalsCount; i++) {
                            Object optional = Array.get(optionals, i);
                            if (optional == null) {
                                continue;
                            }
                            try {
                                Method setEnabledMethod = optional.getClass().getMethod("setEnabled", boolean.class);
                                setEnabledMethod.setAccessible(true);
                                setEnabledMethod.invoke(optional, false);
                            } catch (NoSuchMethodException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }
                    updateFilePathMethod.invoke(installerPanel);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        final JOptionPane pane = optionPane;
        long triggerDeadline = System.currentTimeMillis() + 30000L;
        while (installDialog.isVisible() && System.currentTimeMillis() < triggerDeadline) {
            runOnEventThread(new Runnable() {
                @Override
                public void run() {
                    pane.setValue(JOptionPane.YES_OPTION);
                }
            });
            Thread.sleep(50L);
        }
    }

    private static void watchDialogs(final AtomicBoolean completed) {
        Set<Window> handled = Collections.synchronizedSet(Collections.<Window>newSetFromMap(new IdentityHashMap<Window, Boolean>()));
        while (true) {
            try {
                for (Window window : Window.getWindows()) {
                    if (!(window instanceof JDialog) || !window.isVisible() || handled.contains(window)) {
                        continue;
                    }
                    JDialog dialog = (JDialog) window;
                    if (INSTALL_DIALOG_TITLE.equals(dialog.getTitle())) {
                        continue;
                    }
                    JOptionPane optionPane = findOptionPane(dialog);
                    if (optionPane == null || optionPane.getClass().getName().contains("ProgressMonitor")) {
                        continue;
                    }
                    Object message = optionPane.getMessage();
                    if (message == null) {
                        continue;
                    }
                    handled.add(dialog);
                    String title = dialog.getTitle();
                    System.err.println("[installer] " + (title == null ? "dialog" : title) + ": " + toText(message));
                    if ("Complete".equals(title)) {
                        completed.set(true);
                    }
                    final JDialog dialogToDispose = dialog;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogToDispose.dispose();
                        }
                    });
                }
            } catch (Throwable ignored) {
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static JOptionPane findOptionPane(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JOptionPane) {
                return (JOptionPane) component;
            }
            if (component instanceof Container) {
                JOptionPane optionPane = findOptionPane((Container) component);
                if (optionPane != null) {
                    return optionPane;
                }
            }
        }
        return null;
    }

    private static String toText(Object message) {
        if (message == null) {
            return "";
        }
        if (message instanceof String) {
            return (String) message;
        }
        if (message instanceof Component) {
            return componentText((Component) message);
        }
        if (message instanceof Object[]) {
            StringBuilder builder = new StringBuilder();
            for (Object element : (Object[]) message) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(toText(element));
            }
            return builder.toString();
        }
        return String.valueOf(message);
    }

    private static String componentText(Component component) {
        if (component instanceof JLabel) {
            return ((JLabel) component).getText();
        }
        if (component instanceof JTextComponent) {
            return ((JTextComponent) component).getText();
        }
        if (component instanceof Container) {
            StringBuilder builder = new StringBuilder();
            for (Component child : ((Container) component).getComponents()) {
                String text = componentText(child);
                if (text != null && !text.isEmpty()) {
                    if (builder.length() > 0) {
                        builder.append(' ');
                    }
                    builder.append(text);
                }
            }
            return builder.toString();
        }
        return "";
    }

    private static void runOnEventThread(Runnable runnable) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw e;
        }
    }
}
