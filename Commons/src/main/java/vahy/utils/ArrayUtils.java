package vahy.utils;

public class ArrayUtils {

    public static boolean[] cloneArray(boolean[] vector) {
        boolean[] copy = new boolean[vector.length];
        System.arraycopy(vector, 0, copy, 0, vector.length);
        return copy;
    }

    public static int[] cloneArray(int[] vector) {
        int[] copy = new int[vector.length];
        System.arraycopy(vector, 0, copy, 0, vector.length);
        return copy;
    }

    public static double[] cloneArray(double[] vector) {
        double[] copy = new double[vector.length];
        System.arraycopy(vector, 0, copy, 0, vector.length);
        return copy;
    }

    public static boolean[][] cloneArray(boolean[][] matrix) {
        boolean[][] copy = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = cloneArray(matrix[i]);
        }
        return copy;
    }

    public static int[][] cloneArray(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = cloneArray(matrix[i]);
        }
        return copy;
    }

    public static double[][] cloneArray(double[][] matrix) {
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = cloneArray(matrix[i]);
        }
        return copy;
    }
}
