package org.lajcik.df.platform;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author michal.sienko
 */
class MacPlatformUtils extends PlatformUtils{
    @Override
    public void openFileNatively(File file) {
        try {
            if(file.isDirectory()) {
                Runtime.getRuntime().exec("open \"" + file.getCanonicalPath() + "\"\"");
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Error opening file " + file, e);
        }
    }
}
