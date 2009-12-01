package org.lajcik.df.platform;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author michal.sienko
 */
class WindowsPlatformUtils extends PlatformUtils {
    @Override
    public void openFileNatively(File file) {
        try {
            if (file.isDirectory()) {
                Runtime.getRuntime().exec("cmd /c \"start \"\" \"" + file.getCanonicalPath() + "\"\"");
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Error opening file " + file, e);
        }
    }
}
