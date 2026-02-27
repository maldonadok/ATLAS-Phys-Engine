package org.qft.ml.model.panels;

import org.qft.ml.model.objects.ChargedParticle;
import org.qft.ml.model.objects.Jet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.qft.ml.model.objects.Jet.*;
// ---------- Fast BufferedImage Swing Panel ----------

public class ParticlePanelFast extends JPanel implements MouseMotionListener, MouseWheelListener {

    private static final int UNIT = 100;
    private double angleX = 0;
    private double angleY = 0;
    private double scale = 1.0;
    private int prevX, prevY;
    private static final int GRID_EXTENT = 6;      // +/- range
    private static final double JET_CONE_RADIUS = 0.4; // ΔR
    private boolean etaPhiView = false;
    private static final int CONE_SEGMENTS = 32;
    private boolean showJets = true;
    private boolean showElectrons = true;
    private boolean showMuons = true;
    private boolean showJetCones = true;
    private HoverHit hovered = null;
    private static final int HOVER_RADIUS_PX = 8;
    // ---- cached trig values (updated once per frame) ----
    private double cosX, sinX;
    private double cosY, sinY;
    private final List<Jet> jets;
    private final java.util.List<double[]> electrons;
    private final java.util.List<double[]> muons;
    private static final double AXIS_LENGTH = 1000; // GeV

    // ---- hover support ----
    private static final int HOVER_RADIUS_PX_JETS = 6;
    public boolean minkowskiMode = false;
    private Jet hoveredJet = null;
    private final java.util.List<JetScreenPoint> jetScreenCache = new java.util.ArrayList<>();

    // ---- collision-centered origin (momentum space) ----
    private double originPx = 0;
    private double originPy = 0;
    private double originPz = 0;
    public double simulationTime = 0;

    private List<ChargedParticle> animatedParticles;
    public Timer animationTimer;
    private static final double DT = 0.02; // time step

    public void setAnimatedParticles(List<ChargedParticle> particles) {
        this.animatedParticles = particles;
    }

    public void startAnimation() {

        if (animatedParticles == null || animatedParticles.isEmpty())
            return;

        animationTimer = new Timer(16, e -> {   // ~60 FPS

            simulationTime += DT;

            for (ChargedParticle p : animatedParticles) {

                // ---- store current position for trail ----
                p.trail.addLast(new double[]{p.x, p.y, p.z});
                if (p.trail.size() > 40)
                    p.trail.removeFirst();

                // ---- store spacetime event ----
                p.spacetimeTrail.addLast(
                        new double[]{p.x, simulationTime}
                );
                if (p.spacetimeTrail.size() > 200)
                    p.spacetimeTrail.removeFirst();

                // ---- update position ----
                p.x += p.vx * DT;
                p.y += p.vy * DT;
                p.z += p.vz * DT;
                System.out.println(p.x);
            }

            repaint();
        });

        animationTimer.start();
    }

    public void stopAnimation() {
        if (animationTimer != null)
            animationTimer.stop();
    }

    public void initAnimation() {
        animationTimer = new Timer(16, e -> {

            for (ChargedParticle p : animatedParticles) {

                if (Math.abs(p.x) > 500 ||
                        Math.abs(p.y) > 500 ||
                        Math.abs(p.z) > 500) {

                    p.x = 0;
                    p.y = 0;
                    p.z = 0;
                }

                // Trail
                p.trail.addLast(new double[]{p.x, p.y, p.z});
                if (p.trail.size() > ChargedParticle.MAX_TRAIL_LENGTH) {
                    p.trail.removeFirst();
                }

                simulationTime += DT;
                p.update(DT);

                // Spacetime trail
                p.spacetimeTrail.addLast(new double[]{
                        p.x,
                        simulationTime * ChargedParticle.c
                });

                if (p.spacetimeTrail.size() > 200) {
                    p.spacetimeTrail.removeFirst();
                }
            }

            repaint();
        });
    }

    private static class JetScreenPoint {
        int x, y;
        Jet jet;
    }

    private java.awt.image.BufferedImage buffer;
    private final List<HoverHit> hoverHits = new java.util.ArrayList<>();

    public ParticlePanelFast(java.util.List<Jet> jets,
                             java.util.List<double[]> electrons,
                             java.util.List<double[]> muons) {
        this.jets = jets;
        this.electrons = electrons;
        this.muons = muons;

        computeCollisionOrigin();

        addMouseMotionListener(this);
        addMouseWheelListener(this);

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {

                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_V:
                        etaPhiView = !etaPhiView;
                        break;

                    case java.awt.event.KeyEvent.VK_J:
                        showJets = !showJets;
                        break;

                    case java.awt.event.KeyEvent.VK_E:
                        showElectrons = !showElectrons;
                        break;

                    case java.awt.event.KeyEvent.VK_M:
                        showMuons = !showMuons;
                        break;

                    case java.awt.event.KeyEvent.VK_1:
                        setCameraPreset(CameraPreset.TOP);
                        break;

                    case java.awt.event.KeyEvent.VK_2:
                        setCameraPreset(CameraPreset.SIDE);
                        break;

                    case java.awt.event.KeyEvent.VK_3:
                        setCameraPreset(CameraPreset.ISO);
                        break;

                    case java.awt.event.KeyEvent.VK_C:
                        showJetCones = !showJetCones;
                        break;
                }

                repaint();
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                buffer = null;   // force clean rebuild
                repaint();
            }
        });
    }

    private void renderToBuffer() {

        hoverHits.clear();
        jetScreenCache.clear();

        cosX = Math.cos(angleX);
        sinX = Math.sin(angleX);
        cosY = Math.cos(angleY);
        sinY = Math.sin(angleY);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
            buffer = new BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2 = buffer.createGraphics();

        // ---- HARD RESET the frame ----
        g2.setComposite(AlphaComposite.Src);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

        // ---- now allow alpha blending for particles ----
        g2.setComposite(AlphaComposite.SrcOver);

        if (minkowskiMode) {
            renderMinkowski(g2);
            return;
        }

        int cx = w / 2;
        int cy = h / 2;
        double fov = Math.min(w, h) * 0.9;

        Point origin = new Point(cx, cy);

        drawMomentumAxes(g2, cx, cy, fov);

        Point originPt = project(0, 0, 0, cx, cy, fov);
        g2.setColor(Color.BLACK);
        g2.fillOval(originPt.x - 4, originPt.y - 4, 8, 8);

        if (showJets) {
            jetScreenCache.clear();
            int wBuf = buffer.getWidth();
            int hBuf = buffer.getHeight();
            int argb = JET_COLOR.getRGB();

            int step = jets.size() > 200_000 ? 2 : 1;

            for (int i = 0; i < jets.size(); i += step) {
                Jet j = jets.get(i);

                double x = j.getX() - originPx;
                double y = j.getY() - originPy;
                double z = j.getZ() - originPz;

                // Rotate Y
                double xRot = x * cosY + z * sinY;
                double zRot = -x * sinY + z * cosY;

                // Rotate X
                double yRot = y * cosX - zRot * sinX;
                double zFinal = y * sinX + zRot * cosX;

                if (zFinal < -fov * 0.9) continue;

                int sx = (int)(cx + scale * xRot * fov / (fov + zFinal));
                int sy = (int)(cy - scale * yRot * fov / (fov + zFinal));

                if ((sx | sy) < 0 || sx >= wBuf || sy >= hBuf) continue;

                // ---- draw bolder jet ----
                buffer.setRGB(sx, sy, argb);
                buffer.setRGB(Math.min(sx+1,wBuf-1), sy, argb);
                buffer.setRGB(sx, Math.min(sy+1,hBuf-1), argb);

                if (showJetCones && hoveredJet == j) {
                    drawJetCone(
                            g2,
                            new double[]{j.getX()-originPx, j.getY()-originPy, j.getZ()-originPz},
                            cx, cy,
                            fov,
                            new Color(0, 0, 0, 120)
                    );
                }

                // ---- cache for hover ----
                JetScreenPoint p = new JetScreenPoint();
                p.x = sx;
                p.y = sy;
                p.jet = j;
                jetScreenCache.add(p);
            }
        }
        // ---- Electrons (bold red) ----
        if (showElectrons) {
            for (double[] p : electrons) {
                Point pt = project(p[0], p[1], p[2], cx, cy, fov);

                HoverHit hit = new HoverHit();
                hit.sx = pt.x;
                hit.sy = pt.y;
                hit.px = p[0];
                hit.py = p[1];
                hit.pz = p[2];
                hit.type = "Electron"; // or "Muon"
                hoverHits.add(hit);

                if (animatedParticles == null) {
                    drawPointWithArrow(
                            g2,
                            pt,
                            p[0], p[1], p[2],
                            cx, cy, fov,
                            5,
                            25,
                            2.5f,
                            new Color(255, 0, 0, 220)
                    );
                } else {
                    drawDot(
                            g2,
                            pt,
                            5,
                            new Color(255, 0, 0, 220)
                    );
                }
            }

        // ---- Muons (bold blue) ----
        if (showMuons) {
            for (double[] p : muons) {
                Point pt = project(p[0], p[1], p[2], cx, cy, fov);

                HoverHit hit = new HoverHit();
                hit.sx = pt.x;
                hit.sy = pt.y;
                hit.px = p[0];
                hit.py = p[1];
                hit.pz = p[2];
                hit.type = "Muon"; // or "Muon"
                hoverHits.add(hit);

                if (animatedParticles == null) {
                    drawPointWithArrow(
                            g2,
                            pt,
                            p[0], p[1], p[2],
                            cx, cy, fov,
                            5,
                            25,
                            2.5f,
                            new Color(0, 80, 255, 220)
                    );
                } else {
                    drawDot(
                            g2,
                            pt,
                            5,
                            new Color(0, 80, 255, 220)
                    );
                }
           }
        }

        if (etaPhiView) {
            g2.setColor(new Color(200, 200, 200, 80));
            for (int i = -4; i <= 4; i++) {
                g2.drawLine(cx + i * 120, 0, cx + i * 120, h);
                g2.drawLine(0, cy + i * 120, w, cy + i * 120);
            }
        }

            if (animatedParticles != null) {

                for (ChargedParticle p : animatedParticles) {

                    cx = getWidth() / 2;
                    cy = getHeight() / 2;
                    fov = Math.min(getWidth(), getHeight()) * 0.9;

                    int index = 0;
                    int size = p.trail.size();

                    for (double[] pos : p.trail) {

                        Point trailPoint = project(pos[0], pos[1], pos[2], cx, cy, fov);

                        float alpha = (float) index / size;
                        alpha *= 0.6f; // control fade strength

                        g2.setComposite(AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                alpha
                        ));

                        g2.setColor(Color.MAGENTA);
                        g2.fillOval(trailPoint.x - 3, trailPoint.y - 3, 6, 6);

                        index++;
                    }

                    g2.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            1f
                    ));

                    // Draw main particle
                    Point pt = project(p.x, p.y, p.z, cx, cy, fov);
                    double gamma = p.gamma();
                    double contraction = 1.0 / gamma;

                    double vx = p.vx;
                    double vy = p.vy;

                    // Normalize velocity in screen plane
                    double mag = Math.sqrt(vx*vx + vy*vy);
                    double ux = 0;
                    double uy = 0;

                    if (mag > 0) {
                        ux = vx / mag;
                        uy = vy / mag;
                    }

                    int baseSize = 10;
                    int contractedSize = (int)(baseSize * contraction);

                    // Create transform
                    AffineTransform old = g2.getTransform();

                    g2.translate(pt.x, pt.y);
                    g2.rotate(Math.atan2(uy, ux));

                    g2.fillOval(
                            -baseSize/2,
                            -contractedSize/2,
                            baseSize,
                            contractedSize
                    );

                    g2.setTransform(old);
                }
            }

        g2.dispose();
    }
}
    public void setCameraPreset(CameraPreset preset) {
        switch (preset) {
            case TOP:
                angleX = -Math.PI / 2;  // looking down Z
                angleY = 0;
                break;

            case SIDE:
                angleX = 0;
                angleY = Math.PI / 2;   // looking along X
                break;

            case ISO:
                angleX = -Math.PI / 6;
                angleY = Math.PI / 4;
                break;
        }
        repaint();
    }

    private void renderMinkowski(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        g2.setColor(Color.GRAY);

        // Axes
        g2.drawLine(w/2, 0, w/2, h);     // x = 0
        g2.drawLine(0, h - 50, w, h - 50); // t = 0 baseline

        double xScale = 50;
        double tScale = 50;

        for (ChargedParticle p : animatedParticles) {

            int index = 0;
            int size = p.spacetimeTrail.size();

            for (double[] event : p.spacetimeTrail) {

                double x = event[0];
                double ct = event[1];

                int screenX = (int)(w/2 + x * xScale);
                int screenY = (int)(h - 50 - ct * tScale);

                float alpha = (float) index / size;

                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                alpha
                        )
                );

                g2.setColor(Color.CYAN);
                g2.fillOval(screenX - 3, screenY - 3, 6, 6);

                index++;
            }

            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            1f
                    )
            );
        }
    }

    private double[] normalize(double x, double y, double z) {
        double mag = Math.sqrt(x*x + y*y + z*z);
        if (mag == 0) return new double[]{0,0,0};
        return new double[]{x/mag, y/mag, z/mag};
    }

    private void computeCollisionOrigin() {
        double sx = 0, sy = 0, sz = 0;
        int count = 0;

        for (double[] e : electrons) {
            sx += e[0];
            sy += e[1];
            sz += e[2];
            count++;
        }

        for (double[] m : muons) {
            sx += m[0];
            sy += m[1];
            sz += m[2];
            count++;
        }

        if (count > 0) {
            originPx = sx / count;
            originPy = sy / count;
            originPz = sz / count;
        }
    }


    private void drawAxis(Graphics2D g2,
                          int cx, int cy, double fov,
                          double x1, double y1, double z1,
                          double x2, double y2, double z2,
                          Color color) {

        // Rotate start
        double xs = x1 *  + z1 * sinY;
        double zs = -x1 * sinY + z1 * cosY;
        double ys = y1 * cosX - zs * sinX;
        double zsFinal = y1 * sinX + zs * cosX;

        // Rotate end
        double xe = x2 * cosY + z2 * sinY;
        double ze = -x2 * sinY + z2 * cosY;
        double ye = y2 * cosX - ze * sinX;
        double zeFinal = y2 * sinX + ze * cosX;

        int sx = (int) (cx + scale * xs * fov / (fov + zsFinal));
        int sy = (int) (cy - scale * ys * fov / (fov + zsFinal));
        int ex = (int) (cx + scale * xe * fov / (fov + zeFinal));
        int ey = (int) (cy - scale * ye * fov / (fov + zeFinal));

        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(sx, sy, ex, ey);
    }

    private void drawMomentumAxes(Graphics2D g2, int cx, int cy, double fov) {

        g2.setStroke(new BasicStroke(2.5f));

        // px axis (red)
        drawAxis(
                g2, cx, cy, fov,
                0, 0, 0,
                AXIS_LENGTH, 0, 0,
                new Color(200, 50, 50, 180)
        );

        // py axis (green)
        drawAxis(
                g2, cx, cy, fov,
                0, 0, 0,
                0, AXIS_LENGTH, 0,
                new Color(50, 180, 50, 180)
        );

        // pz axis (blue)
        drawAxis(
                g2, cx, cy, fov,
                0, 0, 0,
                0, 0, AXIS_LENGTH,
                new Color(80, 80, 220, 180)
        );

        drawAxisLabels(g2, cx, cy, fov);
    }

    private void drawAxisLabels(Graphics2D g2, int cx, int cy, double fov) {

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));

        Point pxEnd = project(AXIS_LENGTH, 0, 0, cx, cy, fov);
        Point pyEnd = project(0, AXIS_LENGTH, 0, cx, cy, fov);
        Point pzEnd = project(0, 0, AXIS_LENGTH, cx, cy, fov);

        g2.setColor(Color.RED);
        g2.drawString("px (GeV)", pxEnd.x + 6, pxEnd.y);

        g2.setColor(new Color(0, 140, 0));
        g2.drawString("py (GeV)", pyEnd.x + 6, pyEnd.y);

        g2.setColor(new Color(60, 60, 200));
        g2.drawString("pz (GeV)", pzEnd.x + 6, pzEnd.y);
    }

    private void drawLegend(Graphics2D g2, int w, int h) {

        // ---- Dynamic scaling factors ----
        int padding = Math.max(10, w / 80);
        int fontSize = Math.max(12, h / 40);
        int boxSize = fontSize;
        int lineSpacing = fontSize + 6;

        Font font = new Font("SansSerif", Font.PLAIN, fontSize);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // Legend entries
        String[] labels = {
                "Jets",
                "Muons",
                "Electrons"
        };

        Color[] colors = {
                new Color(0, 0, 0, 180),
                new Color(255, 0, 0, 180),
                new Color(0, 0, 255, 180)
        };

        // ---- Compute legend width dynamically ----
        int maxTextWidth = 0;
        for (String s : labels) {
            maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(s));
        }

        int legendWidth =
                padding * 3 + boxSize + maxTextWidth;

        int legendHeight =
                padding * 2 + labels.length * lineSpacing;

        int x = w - legendWidth - padding;
        int y = h - legendHeight - padding;

        // ---- Background ----
        g2.setColor(new Color(255, 255, 255, 240));
        g2.fillRoundRect(x, y, legendWidth, legendHeight, 12, 12);

        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x, y, legendWidth, legendHeight, 12, 12);

        // ---- Draw entries ----
        for (int i = 0; i < labels.length; i++) {
            int itemY = y + padding + (i + 1) * lineSpacing - 6;

            // Color box
            g2.setColor(colors[i]);
            g2.fillRect(
                    x + padding,
                    itemY - boxSize + 4,
                    boxSize,
                    boxSize
            );

            g2.setColor(Color.BLACK);
            g2.drawRect(
                    x + padding,
                    itemY - boxSize + 4,
                    boxSize,
                    boxSize
            );

            // Text
            g2.drawString(
                    labels[i],
                    x + padding * 2 + boxSize,
                    itemY
            );
        }
    }

    public void zoomIn() {
        scale *= 1.25;
        repaint();
    }

    public void zoomOut() {
        scale /= 1.25;
        repaint();
    }

    private void drawTicksForAxis(Graphics2D g2,
                                  int cx, int cy, double fov,
                                  double ax, double ay, double az,
                                  Color color, String label) {

        g2.setColor(color);

        for (int i = 1; i <= GRID_EXTENT; i++) {
            Point p = project(ax * i, ay * i, az * i, cx, cy, fov);
            g2.fillOval(p.x - 2, p.y - 2, 4, 4);
            g2.drawString(i + " " + UNIT, p.x + 4, p.y - 4);
        }

        // Axis label at far end
        Point end = project(ax * GRID_EXTENT,
                ay * GRID_EXTENT,
                az * GRID_EXTENT,
                cx, cy, fov);
        g2.drawString(label, end.x + 6, end.y + 6);
    }

    private void drawDot(Graphics2D g2,
                         Point center,
                         int radius,
                         Color color) {

        g2.setColor(color);
        g2.fillOval(
                center.x - radius,
                center.y - radius,
                radius * 2,
                radius * 2
        );
    }

    private void drawArrow(Graphics2D g2,
                           Point from,
                           Point to,
                           float thickness,
                           Color color) {

        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        g2.drawLine(from.x, from.y, to.x, to.y);

        // Arrow head
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double angle = Math.atan2(dy, dx);

        int head = (int) (8 + thickness * 2);

        int x1 = (int) (to.x - head * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (to.y - head * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (to.x - head * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (to.y - head * Math.sin(angle + Math.PI / 6));

        Polygon p = new Polygon();
        p.addPoint(to.x, to.y);
        p.addPoint(x1, y1);
        p.addPoint(x2, y2);
        g2.fillPolygon(p);
    }

    private void drawPointWithArrow(Graphics2D g2,
                                    Point center,
                                    double dx, double dy, double dz,
                                    int cx, int cy, double fov,
                                    int pointRadius,
                                    int arrowLength,
                                    float thickness,
                                    Color color) {

        // Draw point
        g2.setColor(color);
        g2.fillOval(
                center.x - pointRadius,
                center.y - pointRadius,
                pointRadius * 2,
                pointRadius * 2
        );

        // Normalize direction
        double[] n = normalize(dx, dy, dz);

        // Small arrow endpoint in 3D
        double ax = n[0] * arrowLength;
        double ay = n[1] * arrowLength;
        double az = n[2] * arrowLength;

        Point arrowEnd = project(ax, ay, az, cx, cy, fov);

        drawArrow(g2, center, arrowEnd, thickness, color);
    }

    private void drawJetCone(Graphics2D g2,
                             double[] p,
                             int cx, int cy, double fov,
                             Color color) {

        double jetEta = eta(p[0], p[1], p[2]);
        double jetPhi = phi(p[0], p[1]);

        Polygon poly = new Polygon();

        for (int i = 0; i < CONE_SEGMENTS; i++) {
            double a = 2 * Math.PI * i / CONE_SEGMENTS;

            double dEta = JET_CONE_RADIUS * Math.cos(a);
            double dPhi = JET_CONE_RADIUS * Math.sin(a);

            double eta = jetEta + dEta;
            double phi = jetPhi + dPhi;

            // Convert (η, φ) back to direction
            double theta = 2 * Math.atan(Math.exp(-eta));
            double x = Math.cos(phi) * Math.sin(theta);
            double y = Math.sin(phi) * Math.sin(theta);
            double z = Math.cos(theta);

            Point pt = project(x * 200, y * 200, z * 200, cx, cy, fov);
            poly.addPoint(pt.x, pt.y);
        }

        g2.setColor(color);
        g2.setStroke(new BasicStroke(
                1.6f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        g2.drawPolygon(poly);
    }

    private void drawPoint(Graphics2D g2,
                           Point center,
                           int radius,
                           Color color) {

        g2.setColor(color);
        g2.fillOval(
                center.x - radius,
                center.y - radius,
                radius * 2,
                radius * 2
        );
    }

    // Math helpers ---------------------------------------

    private double phi(double px, double py) {
        return Math.atan2(py, px);
    }

    private double eta(double px, double py, double pz) {
        double p = Math.sqrt(px*px + py*py + pz*pz);
        return 0.5 * Math.log((p + pz) / (p - pz + 1e-9));
    }

    private double momentumMag(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private Point project(double x, double y, double z,
                         int cx, int cy, double fov) {

        if (etaPhiView) {
            double phi = Math.atan2(y, x);
            double eta = eta(x, y, z);

            int sx = (int) (cx + phi * scale * 120);
            int sy = (int) (cy - eta * scale * 120);
            return new Point(sx, sy);
        }

        // ---- 3D projection (existing) ----
        double xRot = x * cosY + z * sinY;
        double zRot = -x * sinY + z * cosY;

        double yRot = y * cosX - zRot * sinX;
        double zFinal = y * sinX + zRot * cosX;

        int sx = (int) (cx + scale * xRot * fov / (fov + zFinal));
        int sy = (int) (cy - scale * yRot * fov / (fov + zFinal));

        return new Point(sx, sy);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getWidth() <= 0 || getHeight() <= 0) return;

        renderToBuffer();
        g.drawImage(buffer, 0, 0, null);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawLegend(g2, getWidth(), getHeight());

        if (hovered != null) {
            drawHoverTooltip((Graphics2D) g, hovered);
        }

        drawLegend(g2, getWidth(), getHeight());

        if (hoveredJet != null) {
            drawJetTooltip(g2, hoveredJet, prevX, prevY);
        }

        g2.dispose();
    }

    private void drawHoverTooltip(Graphics2D g2, HoverHit h) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        double p = momentumMag(h.px, h.py, h.pz);

        String[] lines = {
                h.type,
                String.format("px = %.2f GeV", h.px),
                String.format("py = %.2f GeV", h.py),
                String.format("pz = %.2f GeV", h.pz),
                String.format("|p| = %.2f GeV", p)
        };

        int padding = 6;
        int lineHeight = fm.getHeight();
        int width = 0;

        for (String s : lines) {
            width = Math.max(width, fm.stringWidth(s));
        }

        int height = lineHeight * lines.length;

        int x = h.sx + 12;
        int y = h.sy - height - 8;

        // keep on screen
        x = Math.min(x, getWidth() - width - padding * 2);
        y = Math.max(y, padding);

        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(x, y, width + padding * 2, height + padding * 2, 10, 10);

        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x, y, width + padding * 2, height + padding * 2, 10, 10);

        g2.setColor(Color.BLACK);
        int ty = y + padding + fm.getAscent();

        for (String s : lines) {
            g2.drawString(s, x + padding, ty);
            ty += lineHeight;
        }
    }

    private void drawJetTooltip(Graphics2D g2, Jet j, int mx, int my) {
        double x = j.getX() - originPx;
        double y = j.getY() - originPy;
        double z = j.getZ() - originPz;
        String[] lines = {
                String.format("Jet"),
                String.format("px: %.2f GeV", x),
                String.format("py: %.2f GeV", y),
                String.format("pz: %.2f GeV", z),
                String.format("|p|: %.2f GeV", momentumMag(x, y, z))
        };

        Font font = new Font("SansSerif", Font.PLAIN, 12);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int padding = 8;
        int width = 0;
        for (String s : lines) width = Math.max(width, fm.stringWidth(s));

        int height = lines.length * fm.getHeight();

        int x1 = mx + 15;
        int y1 = my + 15;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(x1, y1, width + padding*2, height + padding*2, 10, 10);

        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x1, y1, width + padding*2, height + padding*2, 10, 10);

        g2.setColor(Color.BLACK);
        int ty = y1 + padding + fm.getAscent();
        for (String s : lines) {
            g2.drawString(s, x1 + padding, ty);
            ty += fm.getHeight();
        }
    }
 
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(900, 700);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - prevX;
        int dy = e.getY() - prevY;

        angleY += dx * 0.01;
        angleX += dy * 0.01; 

        prevX = e.getX();
        prevY = e.getY();

        repaint();   // ← correct
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();

        int mx = e.getX();
        int my = e.getY();

        // ---- reset hover state EVERY move ----
        hovered = null;
        hoveredJet = null;

        // ---- electrons & muons ----
        int bestDist2 = HOVER_RADIUS_PX * HOVER_RADIUS_PX;

        for (HoverHit h : hoverHits) {
            int dx = h.sx - mx;
            int dy = h.sy - my;
            int d2 = dx * dx + dy * dy;

            if (d2 < bestDist2) {
                bestDist2 = d2;
                hovered = h;
            }
        }

        // ---- jets ----
        int bestDistSq = HOVER_RADIUS_PX_JETS * HOVER_RADIUS_PX_JETS;

        for (JetScreenPoint p : jetScreenCache) {
            int dx = p.x - mx;
            int dy = p.y - my;
            int d2 = dx * dx + dy * dy;

            if (d2 < bestDistSq) {
                bestDistSq = d2;
                hoveredJet = p.jet;
            }
        }

        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scale *= 1 - e.getPreciseWheelRotation() * 0.1;
        repaint();
    }
    public enum CameraPreset {
        TOP, SIDE, ISO
    }

    private static class HoverHit {
        int sx, sy;          // screen coords
        double px, py, pz;   // momentum components
        String type;         // "Jet", "Electron", "Muon"
    }
}


