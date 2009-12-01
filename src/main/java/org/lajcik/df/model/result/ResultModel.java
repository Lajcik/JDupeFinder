package org.lajcik.df.model.result;

import org.lajcik.df.gui.treetable.AbstractTreeTableModel;
import org.lajcik.df.gui.treetable.TreeTableModel;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author michal.sienko
 */
public class ResultModel extends AbstractTreeTableModel {
    private Logger log = Logger.getLogger("JDupeFinder");

    // Names of the columns.
    static protected String[] cNames = {"Name", "Delete?", "Hash", "Size"};
    // prefered size
    static protected int[] cWidth = {400, 70, 220, 190};

    // Types of the columns.
    static protected Class[] cTypes = {TreeTableModel.class, Boolean.class, String.class, Integer.class};

    private DupeCollection root;

    public ResultModel() {
        super(new DupeCollection());
        root = (DupeCollection) super.getRoot();
        root.model = this;
    }

    public ResultNode getChild(Object parent, int index) {
        return (ResultNode)((TreeNode) parent).getChildAt(index);
    }

    public int getChildCount(Object parent) {
        return ((TreeNode) parent).getChildCount();
    }

    public boolean isLeaf(Object node) {
        return ((TreeNode) node).isLeaf();
    }

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class getColumnClass(int column) {
        return cTypes[column];
    }

    public Object getValueAt(Object node, int column) {
        ResultNode item = (ResultNode) node;
        switch (column) {
            case 0:
                return this;
            case 1:
                return item.isChecked();
            case 2:
                return item.getHash();
            case 3:
                return item.getSize();
        }
        return null;
    }

    public void addFile(File file, String hash) {
        root.addFile(file, hash);
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        if (column == 1 && node instanceof ResultNode) {
            ResultNode node1 = (ResultNode) node;
            node1.setChecked((Boolean) aValue);
            if (node instanceof FileGroup) {
                fireTreeNodesChanged(
                        this, new Object[]{root},
                        new int[]{root.getIndex((TreeNode) node)}, new Object[]{node}
                );
            } else {
                FileItem item = (FileItem) node;
                fireTreeNodesChanged(
                        this, new Object[]{root, item.getParent()},
                        new int[]{item.getParent().getIndex(item)}, new Object[]{item.getParent()}
                );

            }
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return column <= 1;
    }

    public int getPreferedWidth(int column) {
        return cWidth[column];
    }

    public void deleteFile(ResultNode node) {
        List<String> errors = new ArrayList<String>();
        if (node instanceof FileItem) {
            log.info("Deleting file: " + node.getFile());
            if (!node.getFile().delete()) {
                log.warning("Could not delete file: " + node.getFile());
                errors.add(node.getFile().toString());
            }

            // precaution to deselect parent if necessary!
            node.setChecked(false);
            FileGroup group = (FileGroup) node.getParent();
            int idx = group.remove((FileItem) node);
            if (group.getChildCount() == 0) {
                idx = root.remove(group);
                fireTreeNodesRemoved(
                        this, new Object[]{root},
                        new int[]{idx}, new Object[]{group}
                );
            } else {
                fireTreeNodesRemoved(
                        this, new Object[]{root, group},
                        new int[]{idx}, new Object[]{node}
                );
            }
        }
        if(!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Could not delete the following files:\n");
            for(String file : errors) {
                sb.append("\t").append(file).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString(),
                    "Error deleting " + errors.size() + " files", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void deleteSelected() {
        int hits = 0;
        for (FileGroup g : new ArrayList<FileGroup>(root.list)) {
            if (g.isChecked()) {
                int i = 0;
                while (i < g.getChildCount()) {
                    FileItem child = g.getChildAt(i);
                    if (child.isChecked()) {
                        deleteFile(child);
                        // don't increment the counter, deletion decrements the size and moves elements!
                        hits++;
                    } else {
                        i++;
                    }
                }
            }
        }
        if (hits > 0) {
            JOptionPane.showMessageDialog(null, "Deleted " + hits + " files",
                    "Finished", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    public void clear() {
        root.clear();
        fireTreeStructureChanged(
                this, new Object[]{root}, new int[]{0}, new Object[]{root}

        );
    }

    public void setRootPath(File path) {
        try {
            root.rootName = path.getCanonicalPath() + File.separatorChar;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @author michal.sienko
     */
    static class DupeCollection implements TreeNode {
        private Map<String, FileGroup> map = new HashMap<String, FileGroup>();
        private List<FileGroup> list = new ArrayList<FileGroup>();
        private ResultModel model;
        private String rootName;

        public ResultNode getChildAt(int childIndex) {
            return list.get(childIndex);
        }

        public int getChildCount() {
            return list.size();
        }

        public TreeNode getParent() {
            return null;
        }

        @SuppressWarnings({"SuspiciousMethodCalls"})
        public int getIndex(TreeNode node) {
            return list.indexOf(node);
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public boolean isLeaf() {
            return false;
        }

        public Enumeration children() {
            return Collections.enumeration(list);
        }

        public int remove(FileGroup group) {
            int idx = list.indexOf(group);
            map.remove(group.getHash());
            list.remove(group);
            return idx;
        }

        public void addFile(File file, String hash) {
            try {
                String name = file.getName();
                if (map.containsKey(hash)) {
                    FileGroup group = map.get(hash);
                    int index = group.add(new FileItem(
                            file.getCanonicalPath().replace(rootName, ""), file, hash, group
                    ));
                    model.fireTreeNodesInserted(
                            this,
                            new Object[]{this, group},
                            new int[]{index},
                            new Object[]{group.getChildAt(index)}
                    );
                } else {
                    FileGroup group = new FileGroup(this, hash);
                    group.add(new FileItem(file.getCanonicalPath().replace(rootName, ""), file, hash, group));
                    map.put(hash, group);
                    int i = 0;
                    while (i < list.size() && list.get(i).getFileName().compareTo(name) < 0) {
                        i++;
                    }
                    list.add(i, group);
                    model.fireTreeNodesInserted(
                            this,
                            new Object[]{this},
                            new int[]{i},
                            new Object[]{group}
                    );
                }
            } catch (IOException e) {
                // ignore
            }

        }

        public void clear() {
            map.clear();
            list.clear();
        }
    }
}
