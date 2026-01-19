package org.qft.ml.model;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.qft.ml.model.objects.Jet;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Parquet3DPlot{
    public static void main(String[] args) throws Exception {
//        if (args.length < 1) {
//            System.err.println("Usage: java Parquet3DSwing <file.parquet>");
//            System.exit(1);
//        }

        String filePathA = "C:\\Datasets\\parquet_output\\Jets.parquet";
        String filePathB = "C:\\Datasets\\parquet_output\\Muons.parquet";
        String filePathC = "C:\\Datasets\\parquet_output\\Electrons.parquet";

        List<Jet> dataA = loadJetMomenta(filePathA);
        List<double[]> dataB = loadMomenta(filePathB);
        List<double[]> dataC = loadMomenta(filePathC);

        // Launch Swing window
        JFrame frame = new JFrame("3D Particle Momenta (Parquet Row-by-Row)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);

        ParticlePanelFast panel =
                new ParticlePanelFast(dataA, dataB, dataC);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        panel.setBounds(0, 0, 1920, 1000);
        layeredPane.add(panel, JLayeredPane.DEFAULT_LAYER);

        JPanel cameraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        cameraPanel.setOpaque(false);

        JButton topBtn  = new JButton("Top");
        JButton sideBtn = new JButton("Side");
        JButton isoBtn  = new JButton("Iso");

        topBtn.addActionListener(e ->
                panel.setCameraPreset(ParticlePanelFast.CameraPreset.TOP));
        sideBtn.addActionListener(e ->
                panel.setCameraPreset(ParticlePanelFast.CameraPreset.SIDE));
        isoBtn.addActionListener(e ->
                panel.setCameraPreset(ParticlePanelFast.CameraPreset.ISO));

        cameraPanel.add(topBtn);
        cameraPanel.add(sideBtn);
        cameraPanel.add(isoBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(panel, BorderLayout.CENTER);
        root.add(cameraPanel, BorderLayout.SOUTH);

        cameraPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        cameraPanel.setBackground(new Color(255, 255, 255, 220));
        cameraPanel.setOpaque(true);

        frame.setContentPane(root);

        // Zoom buttons
        JButton zoomInBtn = new JButton("+");
        JButton zoomOutBtn = new JButton("−");

        zoomInBtn.setFocusable(false);
        zoomOutBtn.setFocusable(false);

        zoomInBtn.addActionListener(e -> panel.zoomIn());
        zoomOutBtn.addActionListener(e -> panel.zoomOut());

        int buttonSize = 42;
        int margin = 15;

        zoomInBtn.setBounds(
                1980 - buttonSize - margin,
                1080 - 3 * buttonSize - margin,
                buttonSize,
                buttonSize
        );

        zoomOutBtn.setBounds(
                1980 - buttonSize - margin,
                1080 - 2 * buttonSize - margin,
                buttonSize,
                buttonSize
        );

        layeredPane.add(zoomInBtn, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(zoomOutBtn, JLayeredPane.PALETTE_LAYER);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();

                zoomInBtn.setBounds(
                        w - buttonSize - margin,
                        h - 3 * buttonSize - margin,
                        buttonSize,
                        buttonSize
                );

                zoomOutBtn.setBounds(
                        w - buttonSize - margin,
                        h - 2 * buttonSize - margin,
                        buttonSize,
                        buttonSize
                );
            }
        });
        frame.add(layeredPane);
        frame.setVisible(true);
    }

    private static List<Jet> loadJetMomenta(String filePathA) throws Exception {
        List<Jet> data = new ArrayList<>();
        File file = new File(filePathA);
        HadoopInputFile inputFile =
                HadoopInputFile.fromPath(
                        new Path(file.getAbsolutePath()),
                        new Configuration()
                );
        try (ParquetReader<GenericRecord> reader =
                     AvroParquetReader.<GenericRecord>builder(inputFile).build()) {
            GenericRecord rec;
            while ((rec = reader.read()) != null) {
                double px = getDoubleOrDefault(rec, "px", Double.NaN);
                double py = getDoubleOrDefault(rec, "py", Double.NaN);
                double pz = getDoubleOrDefault(rec, "pz", Double.NaN);

                if (Double.isFinite(px) && Double.isFinite(py) && Double.isFinite(pz)) {
                    data.add(new Jet(px,py,pz));
                }
            }
            return data;
        }
    }

    private static List<double[]> loadMomenta(String filePath) throws Exception {

        List<double[]> data = new ArrayList<>();

        File file = new File(filePath);
        HadoopInputFile inputFile =
                HadoopInputFile.fromPath(
                        new Path(file.getAbsolutePath()),
                        new Configuration()
                );

        try (ParquetReader<GenericRecord> reader =
                     AvroParquetReader.<GenericRecord>builder(inputFile).build()) {

            GenericRecord rec;
            while ((rec = reader.read()) != null) {

                double px = getDoubleOrDefault(rec, "px", Double.NaN);
                double py = getDoubleOrDefault(rec, "py", Double.NaN);
                double pz = getDoubleOrDefault(rec, "pz", Double.NaN);

                if (Double.isFinite(px) && Double.isFinite(py) && Double.isFinite(pz)) {
                    data.add(new double[]{px, py, pz});
                }
            }
        }
        return data;
    }

    private static double getDoubleOrDefault(GenericRecord record,
                                             String field,
                                             double defaultValue) {
        try {
            if (record == null) {
                return defaultValue;
            }

            Object value = record.get(field);
            if (value == null) {
                return defaultValue;
            }

            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }

            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

}

