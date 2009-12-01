package org.lajcik.df.gui;

import org.lajcik.df.platform.PlatformUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author michal.sienko
 */
public class ImagePanel extends JPanel {
    public static final int MAX_X = 150;
    public static final int MAX_Y = 150;

    private BufferedImage image;
    private int width;
    private int height;
    private int x;
    private int y;

    private Icon icon;

    public ImagePanel() {
        setBorder(new LineBorder(Color.BLACK));
    }

    public void updateImage(File file) {
        icon = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            image = null;
        }
        if (image != null) {
            if (image.getWidth() > image.getHeight()) {
                width = getWidth();
                double ratio = (double) image.getWidth() / getWidth();
                height = (int) (image.getHeight() / ratio);
                x = 0;
                y = (getHeight() - height) / 2;
            } else {
                height = getHeight();
                double ratio = (double) image.getHeight() / getWidth();
                width = (int) (image.getWidth() / ratio);
                y = 0;
                x = (getWidth() - width) / 2;

            }
        } else {
            icon = PlatformUtils.getInstance().getLargeIcon(file);
            x = (getWidth() - icon.getIconWidth()) / 2;
            y = (getHeight() - icon.getIconHeight()) / 2;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (icon != null) {
            g.setColor(this.getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            icon.paintIcon(this, g, x, y);
        } else {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    @Override
    public void paintComponents(Graphics g) {
        paintComponent(g);
    }
}
