package hu.akarnokd.linearprogramming;

import java.util.*;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public final class TaskTimeDetect {

    private TaskTimeDetect() {}

    public static void main(String[] args) {
        double[] generatorTimes = { 5, 6, 7, 8 };
        Random rng = new Random(0L);

        double[] times = new double[40];
        int[] types = new int[40];
        double tsum = 0;
        int dataLen = times.length;
        for (int i = 0; i < dataLen; i++) {
            int y = rng.nextInt(generatorTimes.length);
            double t = generatorTimes[y];
            tsum += t;
            times[i] = tsum;
            types[i] = y;
            if (i * 2 == dataLen) {
                tsum += 0;
            }
        }
        System.out.println("Total time: " + tsum);

        for (int i = 1; i < dataLen; i++) {
            double x = rng.nextDouble();
            if (x < 0.2) {
                times[i - 1] = times[i] - 1;
                if (x < 0.1) {
                    int t0 = types[i - 1];
                    types[i - 1] = types[i];
                    types[i] = t0;
                }
            }
        }

        List<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 1; i < dataLen; i++) {
            double[] coeffs = new double[generatorTimes.length + 2 * (dataLen - 1)];

            for (int j = 1; j <= i; j++) {
                coeffs[types[j]]++;
            }
            coeffs[generatorTimes.length + 2 * (i - 1)] = 1;
            coeffs[generatorTimes.length + 2 * (i - 1) + 1] = -1;

            double diff = times[i] - times[0];

            constraints.add(new LinearConstraint(coeffs, Relationship.EQ, diff));
        }

        double[] objCoeffs = new double[generatorTimes.length + 2 * (dataLen - 1)];
        for (int i = generatorTimes.length; i < objCoeffs.length; i++) {
            objCoeffs[i] = 1;
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(objCoeffs, 0);

        SimplexSolver solver = new SimplexSolver();

        PointValuePair result = solver.optimize(f, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));

        System.out.printf("%.3f%n", result.getValue());
        double[] res = result.getFirst();
        for (int i = 0; i < res.length; i++) {
            if (i < generatorTimes.length) {
                System.out.printf("[Task %s] %.3f%n", i, res[i]);
            } else {
                System.out.printf("[%s] %.3f%n", i, res[i]);
            }
        }

        tsum = 0;
        for (int i = 0; i < dataLen; i++) {
            tsum += res[types[i]];
        }
        System.out.println("Restored total length: " + tsum);
    }
}
