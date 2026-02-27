package org.qft.ml.model.calc;

import org.qft.ml.model.objects.ChargedParticle;

import java.util.List;

public class EMFieldCalculator {

    private static final double K = 8.9875517923e9; // 1/(4πϵ0)
    private static final double ELECTRON_CHARGE = -1.602e-19;



    public static double[][][] computeField(
            List<double[]> electrons,
            int gridSize,
            double range) {

        double[][][] field = new double[gridSize][gridSize][2];

        double step = (2 * range) / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {

                double x = -range + i * step;
                double y = -range + j * step;

                double Ex = 0;
                double Ey = 0;

                for (double[] e : electrons) {

                    double dx = x - e[0];
                    double dy = y - e[1];
                    double r2 = dx*dx + dy*dy + 1e-9;
                    double r3 = Math.pow(r2, 1.5);

                    Ex += K * ELECTRON_CHARGE * dx / r3;
                    Ey += K * ELECTRON_CHARGE * dy / r3;
                }

                field[i][j][0] = Ex;
                field[i][j][1] = Ey;
            }
        }

        return field;
    }
}
