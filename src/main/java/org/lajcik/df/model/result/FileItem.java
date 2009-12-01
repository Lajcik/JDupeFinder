package org.lajcik.df.model.result;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.Enumeration;

/**
 * @author michal.sienko
 */
public class FileItem implements ResultNode{
    private File file;
    private String hash;
    boolean checked;
    private FileGroup parent;
    private String name;

    public FileItem(String name, File file, String hash, FileGroup fileGroup) {
        this.name = name;
        this.parent = fileGroup;
        this.file = file;
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        parent.updateCheckMark();
    }

    public String getHash() {
        return hash;
    }

    public Long getSize() {
        return file.length();
    }

    public File getFile() {
        return file;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        return null;
    }

    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    public int getChildCount() {
        return 0;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return 0;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public String toString() {
        return getName();
    }
}
