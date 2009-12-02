package org.lajcik.df.service;

import org.jdesktop.swingx.JXErrorDialog;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author michal.sienko
 */
public class FileSearchTask implements Runnable {
    private File root;
    private FileLocatorListener listener;
    private MessageDigest md;
    private JProgressBar progress;

    private static FileFilter fileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isFile();
        }
    };
    private static FileFilter dirFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory() && !".".equals(pathname.getName()) && !"..".equals(pathname.getName());
        }
    };
    private final Runnable updateProgress = new Runnable() {
        public void run() {
            progress.setValue(progress.getValue() + 1);
        }
    };
    private final Runnable abortProgress = new Runnable() {
        public void run() {
            progress.setString("Aborted");
            listener.finished();
        }
    };
    private final Runnable errorProgress = new Runnable() {
        public void run() {
            progress.setString("Error");
            listener.finished();
        }
    };
    private final Runnable finishedProgress = new Runnable() {
        public void run() {
            progress.setValue(progress.getMaximum());
            progress.setString("Finished");
            listener.finished();
        }
    };


    public FileSearchTask(File root, FileLocatorListener listener, JProgressBar progress) {
        this.root = root;
        this.listener = listener;
        this.progress = progress;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

    }

    public void run() {
        int i = countFiles(root);
        progress.setMaximum(i);
        progress.setValue(0);
        progress.setString(null);
        try {
            search(root);
            invoke(finishedProgress);
        } catch (InterruptedException e) {
            SwingUtilities.invokeLater(abortProgress);
        }
    }

    private int countFiles(File dir) {
        int x = dir.listFiles(fileFilter).length;
        for (File aDir : dir.listFiles(dirFilter)) {
            x += countFiles(aDir);
        }
        return x;
    }

    private void search(File dir) throws InterruptedException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                search(file);
                continue;
            }
            md.reset();
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                byte[] buf = new byte[2048];
                int read;
                while ((read = in.read(buf)) != -1) {
                    md.update(buf, 0, read);
                }
                byte[] digest = md.digest();
                invoke(updateProgress);

                listener.addFile(file, convertToHex(digest));
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            } catch (FileNotFoundException e) {
                SwingUtilities.invokeLater(errorProgress);
                throw new IllegalStateException(e);
            } catch (IOException e) {
                JXErrorDialog.showDialog(null, "Error reading file " + file.getName(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte aData : data) {
            int halfbyte = (aData >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = aData & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private void invoke(Runnable task) throws InterruptedException {
        try {
            SwingUtilities.invokeAndWait(task);
        } catch (InvocationTargetException e) {
            //
        }
    }

}
