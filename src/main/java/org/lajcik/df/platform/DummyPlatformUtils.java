package org.lajcik.df.platform;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author michal.sienko
 */
class DummyPlatformUtils extends PlatformUtils{
    private String os = System.getProperty("os.name");

    @Override
    public void openFileNatively(File file) {
        log.warning("Don't know how to open file on " + os);
    }
}
