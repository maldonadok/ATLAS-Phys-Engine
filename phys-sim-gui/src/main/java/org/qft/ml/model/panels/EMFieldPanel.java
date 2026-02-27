package org.qft.ml.model.panels;

import javax.swing.*;
import java.awt.*;

public class EMFieldPanel extends JPanel {

    private final double[][][] field;

    public EMFieldPanel(double[][][] field) {
        this.field = field;
        setPreferredSize(new Dimension(800, 800));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        int gridSize = field.length;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {

                double Ex = field[i][j][0];
                double Ey = field[i][j][1];

                double magnitude = Math.sqrt(Ex*Ex + Ey*Ey);
                if (magnitude == 0) continue;

                double scale = 20 / magnitude; // normalize arrows

                int x = i * w / gridSize;
                int y = j * h / gridSize;

                int dx = (int)(Ex * scale);
                int dy = (int)(Ey * scale);

                g2.drawLine(x, y, x + dx, y - dy);
            }
        }
    }
}
