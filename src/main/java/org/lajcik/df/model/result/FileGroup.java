package org.lajcik.df.model.result;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author michal.sienko
 */
public class FileGroup implements ResultNode {
    private ResultModel.DupeCollection parent;
    private String hash;
    private List<FileItem> files;
    private boolean checked;

    public FileGroup(ResultModel.DupeCollection parent, String hash) {
        this.parent = parent;
        this.hash = hash;
        this.files = new ArrayList<FileItem>();
    }

    public int remove(FileItem node) {
        int idx = files.indexOf(node);
        files.remove(node);
        return idx;
    }

    public int add(FileItem file) {
        int index = 0;
        if(files.size() > 0) {
            while (index < files.size() && files.get(index).getName().compareTo(file.getName()) < 0) {
                index ++;
            }
        }
        files.add(index, file);
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileGroup fileGroup = (FileGroup) o;

        return hash.equals(fileGroup.hash);

    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    public String getName() {
        return files.get(0).getName() + " (" + files.size() + ")";
    }

    public String getFileName() {
        return files.get(0).getName();
    }

    public boolean isChecked() {
        return checked;
    }

    public String getHash() {
        return hash;
    }

    public Long getSize() {
        return null;
    }

    public File getFile() {
        return files.get(0).getFile();
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if(checked && files.size() > 1) {
            files.get(0).checked = false;
            for(int i = 1; i < files.size(); i++ ) {
                files.get(i).checked = true;
            }
        } else {
            for(FileItem item : files) {
                item.checked = checked;
            }            
        }
    }

    void updateCheckMark() {
        for(FileItem item : files) {
            if(item.isChecked()) {
                checked = true;
                return;
            }
        }
        checked = false;
    }

    public boolean isLeaf() {
        return false;
    }

    public Enumeration children() {
        return Collections.enumeration(files);
    }

    public FileItem getChildAt(int childIndex) {
        return files.get(childIndex);
    }

    public int getChildCount() {
        return files.size();
    }

    public TreeNode getParent() {
        return parent;
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public int getIndex(TreeNode node) {
        return files.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public String toString() {
        return getName();
    }
}
