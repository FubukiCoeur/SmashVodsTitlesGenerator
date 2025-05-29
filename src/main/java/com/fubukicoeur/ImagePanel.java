package com.fubukicoeur;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    private Image img;

    public ImagePanel() {
        super();
        this.img = null;
    }

    public ImagePanel(Image im) {
        super();
        this.img = im;
    }

    public Image getImage() {
        return this.img;
    }

    public void setImage(Image im) {
        this.img = im;
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (img != null)
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
    }

}
