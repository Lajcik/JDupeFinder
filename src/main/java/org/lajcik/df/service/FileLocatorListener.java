package org.lajcik.df.service;

import java.io.File;

/**
 * @author michal.sienko
 */
public interface FileLocatorListener {
    public void addFile(File file, String hash);
    public void finished();
}
