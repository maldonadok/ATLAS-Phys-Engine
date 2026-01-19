package org.qft.ml.model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Graph3DPanel extends JPanel {

    private double angleX = Math.toRadians(30);
    private double angleY = Math.toRadians(45);
    private int prevX, prevY;

    private final double scale = 80;
    private final double cameraDistance = 6;

    public Graph3DPanel() {
        setBackground(Color.BLACK);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - prevX;
                int dy = e.getY() - prevY;

                angleY += dx * 0.01;
                angleX += dy * 0.01;

                prevX = e.getX();
                prevY = e.getY();
                repaint();
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        drawAxes(g2, cx, cy);
        drawSurface(g2, cx, cy);
    }

    private void drawAxes(Graphics2D g2, int cx, int cy) {
        drawLine3D(g2, -3, 0, 0, 3, 0, 0, Color.RED, cx, cy);   // X
        drawLine3D(g2, 0, -3, 0, 0, 3, 0, Color.GREEN, cx, cy); // Y
        drawLine3D(g2, 0, 0, -3, 0, 0, 3, Color.BLUE, cx, cy); // Z
    }

    private void drawSurface(Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.ORANGE);

        for (double x = -2; x <= 2; x += 0.2) {
            for (double y = -2; y <= 2; y += 0.2) {
                double z = Math.sin(x) * Math.cos(y);

                Point p = project(x, y, z);
                g2.fillOval(cx + p.x - 2, cy - p.y - 2, 4, 4);
            }
        }
    }

    private Point project(double x, double y, double z) {
        // Rotate around X
        double y1 = y * Math.cos(angleX) - z * Math.sin(angleX);
        double z1 = y * Math.sin(angleX) + z * Math.cos(angleX);

        // Rotate around Y
        double x2 = x * Math.cos(angleY) + z1 * Math.sin(angleY);
        double z2 = -x * Math.sin(angleY) + z1 * Math.cos(angleY);

        // Perspective
        double perspective = cameraDistance / (cameraDistance - z2);

        int px = (int) (x2 * scale * perspective);
        int py = (int) (y1 * scale * perspective);

        return new Point(px, py);
    }

    private void drawLine3D(Graphics2D g2,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            Color color, int cx, int cy) {
        g2.setColor(color);
        Point p1 = project(x1, y1, z1);
        Point p2 = project(x2, y2, z2);
        g2.drawLine(cx + p1.x, cy - p1.y, cx + p2.x, cy - p2.y);
    }
}




