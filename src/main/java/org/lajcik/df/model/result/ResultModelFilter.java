package org.lajcik.df.model.result;

import org.lajcik.df.gui.treetable.AbstractTreeTableModel;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author michal.sienko
 */
public class ResultModelFilter extends AbstractTreeTableModel implements TreeModelListener {
    private ResultModel model;
    private List<Integer> view = new ArrayList<Integer>();

    public ResultModelFilter(ResultModel model) {
        super(model.getRoot());
        this.model = model;

        model.addTreeModelListener(this);
        reset();
    }

    private void reset() {
        view.clear();
        int count = model.getChildCount(root);
        for (int i = 0; i < count; i++) {
            ResultNode node = model.getChild(root, i);
            if (model.getChildCount(node) > 1) {
                view.add(i);
            }
        }
    }

    public int getColumnCount() {
        return model.getColumnCount();
    }

    public Class getColumnClass(int column) {
        return model.getColumnClass(column);
    }

    public boolean isLeaf(Object node) {
        return model.isLeaf(node);
    }

    public boolean isCellEditable(Object node, int column) {
        return model.isCellEditable(node, column);
    }

    public void setValueAt(Object aValue, Object node, int column) {
        model.setValueAt(aValue, node, column);
    }

    public String getColumnName(int column) {
        return model.getColumnName(column);
    }

    public Object getValueAt(Object node, int column) {
        return model.getValueAt(node, column);
    }

    public Object getChild(Object parent, int index) {
        if (parent == root) {
            return model.getChild(parent, view.get(index));
        } else {
            return model.getChild(parent, index);
        }
    }

    public int getChildCount(Object parent) {
        if (parent == root) {
            return view.size();
        } else {
            return model.getChildCount(parent);
        }
    }

    public void treeNodesChanged(TreeModelEvent e) {
        fireTreeNodesChanged(e.getSource(), e.getPath(), e.getChildIndices(), e.getChildren());
    }

    public void treeNodesInserted(TreeModelEvent e) {
        fireTreeNodesInserted(e.getSource(), e.getPath(), e.getChildIndices(), e.getChildren());
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        fireTreeNodesRemoved(e.getSource(), e.getPath(), e.getChildIndices(), e.getChildren());
    }

    public void treeStructureChanged(TreeModelEvent e) {
        fireTreeStructureChanged(e.getSource(), e.getPath(), e.getChildIndices(), e.getChildren());
    }

    @Override
    protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        super.fireTreeNodesChanged(source, path, childIndices, children);
    }

    private int insert(int idx) {
        for (int i = 0; i < view.size(); i++) {
            if (view.get(i) > idx) {
                view.add(i, idx);
                return i;
            }
        }
        int i = view.size();
        view.add(i, idx);
        return i;
    }

    @Override
    protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
        if (path.length == 1) { // inserted filegroup
            if (model.getChildCount(children[0]) > 1) {
                super.fireTreeNodesInserted(source, path, new int[]{insert(childIndices[0])}, children);
            } else {
                // shift the apropriate indexes in the view
                int idx = childIndices[0];
                for (int i = 0; i < view.size(); i++) {
                    if (view.get(i) > idx) {
                        view.set(i, view.get(i) + 1);
                    }
                }
            }
        } else if (path.length == 2) { // inserted a FileItem!
            FileItem item = (FileItem) children[0];
            int idx = model.getIndexOfChild(root, item.getParent());
            if (view.contains(idx)) {
                // insert the FileItem
                super.fireTreeNodesInserted(source, path, childIndices, children);
            } else if (item.getParent().getChildCount() > 1) {
                // if the FileGroup qualifies for the view now - insert it
                int i = insert(idx);
                super.fireTreeNodesInserted(source, new Object[]{root},
                        new int[]{i}, new Object[]{item.getParent()});
            }
        }
    }

    @Override
    protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        if (path.length == 1) { // removed a FileGroup
            int idx = view.indexOf(childIndices[0]);
            if (idx >= 0) {
                // remove only if it was on the view!
                view.remove(idx);
                super.fireTreeNodesRemoved(source, path, new int[]{idx}, children);
            } else {
                // shift the apropriate indexes in the view
                idx = childIndices[0];
                for (int i = 0; i < view.size(); i++) {
                    if (view.get(i) > idx) {
                        view.set(i, view.get(i) - 1);
                    }
                }
            }
        } else if (path.length == 2) { // removed a FileItem!
            FileItem item = (FileItem) children[0];
            int idx = model.getIndexOfChild(root, item.getParent());
            if (idx >= 0) {
                if (item.getParent().getChildCount() <= 1) {
                    // if the FileGroup doesn't qualify for display anymore - remove it
                    idx = view.indexOf(idx);
                    view.remove(idx);
                    super.fireTreeNodesRemoved(source, new Object[]{root}, new int[]{idx}, new Object[]{item.getParent()});
                } else {
                    // remove the FileItem
                    super.fireTreeNodesRemoved(source, path, childIndices, children);
                }
            }
        } else {
            super.fireTreeNodesRemoved(source, path, childIndices, children);
        }
    }

    @Override
    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        reset();
        super.fireTreeStructureChanged(source, new Object[]{root}, new int[]{}, new Object[]{});
    }
}
