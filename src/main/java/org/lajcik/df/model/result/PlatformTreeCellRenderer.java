package org.lajcik.df.model.result;

import org.lajcik.df.platform.PlatformUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author michal.sienko
 */
public class PlatformTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                  boolean expanded, boolean leaf,
                                                  int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if(value instanceof ResultNode) {
            ResultNode node = (ResultNode) value;
            if(node instanceof FileGroup) {
                setIcon(PlatformUtils.getInstance().getIcon(node.getFile().getParentFile()));                
            } else {
                setIcon(PlatformUtils.getInstance().getIcon(node.getFile()));
            }
        }
        return this;
    }


}
