package org.lajcik.df.model.result;

import javax.swing.tree.TreeNode;
import java.io.File;

/**
 * @author michal.sienko
 */
public interface ResultNode extends TreeNode{
    public String getName();
    public boolean isChecked();
    public String getHash();
    public Long getSize();
    public File getFile();
    public void setChecked(boolean checked);
}
