package org.lajcik.df.platform;

import sun.awt.shell.ShellFolder;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

/**
 * @author michal.sienko
 */
public abstract class PlatformUtils {
    private static PlatformUtils instance;
    protected Logger log = Logger.getLogger("PlatformUtils");

    static {
        if (System.getProperty("os.name").startsWith("Windows")) {
            instance = new WindowsPlatformUtils();
        } else if (System.getProperty("os.name").indexOf("Linux") != -1) {
            // Linux-specific solution here: gnome-open, xdg-open, see
        } else if (System.getProperty("os.name").indexOf("Mac") != -1) {
            instance = new MacPlatformUtils();
        } else {
            instance = new DummyPlatformUtils();
        }
    }

    protected PlatformUtils() {
    }

    public static PlatformUtils getInstance() {
        return instance;
    }

    /**
     * Open file/directory natively with the operating system using the default application.
     * <p/>
     * This method will fail silently on any errors (no exceptions are thrown)
     *
     * @param file file
     */
    public abstract void openFileNatively(File file);

    public Icon getIcon(File file) {
        FileSystemView view = FileSystemView.getFileSystemView();
        return view.getSystemIcon(file);
    }

    public Icon getLargeIcon(File file) {
        ShellFolder shellFolder = null;
        try {
            shellFolder = ShellFolder.getShellFolder(file);
            return new ImageIcon(shellFolder.getIcon(true));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
