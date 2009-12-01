package org.lajcik.df.gui;

import org.lajcik.df.gui.treetable.JTreeTable;
import org.lajcik.df.model.result.*;
import org.lajcik.df.platform.PlatformUtils;
import org.lajcik.df.service.FileLocatorListener;
import org.lajcik.df.service.FileSearchTask;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author michal.sienko
 */
public class ResultForm implements FileLocatorListener {
    private JPanel resultPanel;
    private JTreeTable resultTable;
    private ImagePanel imagePreview;
    private JLabel labelFileName;
    private JLabel labelFileSize;
    private JLabel labelHash;
    private JButton openDirectoryButton;
    private JButton openFileButton;
    private JButton deleteFileButton;
    private JButton newSearchButton;
    private JButton deleteSelectedButton;
    private JProgressBar progressBar;
    private JToggleButton hideNonDupes;

    private ResultModel model;

    private final ExecutorService executor;
    private Future future;
    private ResultForm.InfoPanelMonitor infoPanelMonitor;

    public ResultForm(ExecutorService executorObj) {
        this.executor = executorObj;
        hideNonDupes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                filter.setActive(hideNonDupes.isSelected());
                if (hideNonDupes.isSelected()) {
                    resultTable.setModel(new ResultModelFilter(model));
                } else {
                    resultTable.setModel(model);
                }
            }
        });
        openDirectoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResultNode node = (ResultNode) resultTable.getTree().getLastSelectedPathComponent();
                if (node != null) {
                    File f = node.getFile().isDirectory() ? node.getFile() : node.getFile().getParentFile();
                    PlatformUtils.getInstance().openFileNatively(f);
                }
            }
        });
        openFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResultNode node = (ResultNode) resultTable.getTree().getLastSelectedPathComponent();
                if (node != null) {
                    PlatformUtils.getInstance().openFileNatively(node.getFile());
                }
            }
        });
        deleteFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResultNode node = (ResultNode) resultTable.getTree().getLastSelectedPathComponent();
                model.deleteFile(node);
            }
        });
        newSearchButton.addActionListener(new ActionListener() {
            public synchronized void actionPerformed(ActionEvent e) {
                if (future != null) {
                    future.cancel(true);
                    future = null;
                }
                if (newSearchButton.getText().equals("Stop")) {
                    newSearchButton.setText("Start");
                } else {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File root = chooser.getSelectedFile();
                        FileSearchTask task = new FileSearchTask(root, ResultForm.this, progressBar);
                        model.clear();
                        model.setRootPath(root);
                        future = executor.submit(task);
                        newSearchButton.setText("Stop");
                    }
                }
            }
        });
        deleteSelectedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.deleteSelected();
                infoPanelMonitor.invalidate();
            }
        });
    }

    public JComponent getPanel() {
        return resultPanel;
    }

    public void addFile(final File file, final String hash) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                model.addFile(file, hash);
            }
        });
    }

    public void finished() {
        newSearchButton.setText("New Search");
        future = null;
    }

    private void createUIComponents() {
        model = new ResultModel();
        resultTable = new JTreeTable(model);
        resultTable.setRootVisible(false);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getTree().setCellRenderer(new PlatformTreeCellRenderer());
        Enumeration<TableColumn> it = resultTable.getColumnModel().getColumns();
        int i = 0;
        while (it.hasMoreElements()) {
            it.nextElement().setPreferredWidth(
                    model.getPreferedWidth(i++)
            );
        }

        infoPanelMonitor = new InfoPanelMonitor();
        resultTable.getSelectionModel().addListSelectionListener(infoPanelMonitor);
    }

    private class InfoPanelMonitor implements ListSelectionListener {
        private int lastIndex = -1;

        public void invalidate() {
            lastIndex = -1;
        }

        public void valueChanged(ListSelectionEvent e) {
            // discard adjusting events
            if (e.getValueIsAdjusting()) {
                return;
            }
            // determine new selection
            int idx;
            if (resultTable.getSelectionModel().isSelectedIndex(e.getFirstIndex())) {
                idx = e.getFirstIndex();
            } else if (resultTable.getSelectionModel().isSelectedIndex(e.getLastIndex())) {
                idx = e.getLastIndex();
            } else {
                return;
            }

            // discard if no real change
            if (lastIndex == idx) {
                return;
            }
            lastIndex = idx;

            ResultNode node = (ResultNode) resultTable.getTree().getLastSelectedPathComponent();
            imagePreview.updateImage(node.getFile());
            labelFileName.setText(node instanceof FileItem ? node.getFile().toString() : "<GROUP>");
            String tmp = "N/A";
            if (node.getSize() != null) {
                long size = node.getSize() / 1024;
                if (size > 1024) {
                    size = size / 1024;
                    tmp = "Mb";
                } else {
                    tmp = "Kb";
                }
                tmp = String.format("%,1d %s", size, tmp);
            }
            labelFileSize.setText(tmp);
            labelHash.setText(node.getHash());
            if (node instanceof FileItem) {
                openFileButton.setEnabled(true);
                openDirectoryButton.setEnabled(true);
                deleteFileButton.setEnabled(true);
            } else {
                openFileButton.setEnabled(false);
                openDirectoryButton.setEnabled(false);
                deleteFileButton.setEnabled(false);
            }
        }

    }
}
