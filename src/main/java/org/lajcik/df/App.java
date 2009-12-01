package org.lajcik.df;

import org.lajcik.df.gui.ResultForm;
import org.lajcik.df.service.FileSearchTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author michal.sienko
 */
public class App {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        ResultForm gui = new ResultForm(executor);
        frame.setTitle("JDupeFinder");
        frame.add(gui.getPanel());
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(900, 300));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                executor.shutdownNow();
                System.exit(0);                
            }
        });


    }
}
