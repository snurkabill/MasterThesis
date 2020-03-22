package vahy.thirdpartyprototype.ClpMultithread;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class Worker implements Callable<Worker> {

    private final int id;
    private final CLPTestModel clp;
    private double result;

    public static double[][] deepCopy(double[][] original) {
        if (original == null) {
            return null;
        }

        final double[][] result = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    public Worker(double[][] input, int id) {
        this.id = id;
        this.clp = new CLPTestModel(Worker.deepCopy(input));
    }

    public void run() {
        result = clp.solve();
    }

    public double getSolution() {
        return result;
    }

    public double[][] getAssigemment() {
        return clp.getAssignments();
    }

    @Override
    public Worker call() throws Exception {
        run();
        return this;
    }
}
