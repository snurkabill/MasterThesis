package vahy.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayUtils {

    public static <T> boolean hasRectangleShape(List<List<T>> matrix) {
        return matrix.stream().collect(Collectors.groupingBy(List::size)).values().size() == 1;
    }

    // avoiding generics here since we want to use primitive types

    public static boolean hasRectangleShape(boolean[][] matrix) {
        return Arrays.stream(matrix).collect(Collectors.groupingBy(x -> x.length)).values().size() == 1;
    }

    public static boolean hasRectangleShape(int[][] matrix) {
        return Arrays.stream(matrix).collect(Collectors.groupingBy(x -> x.length)).values().size() == 1;
    }

    public static boolean hasRectangleShape(double[][] matrix) {
        return Arrays.stream(matrix).collect(Collectors.groupingBy(x -> x.length)).values().size() == 1;
    }

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

    public static boolean[] alignMatrixToVector(boolean[][] matrix) {
        if(!hasRectangleShape(matrix)) {
            throw new IllegalArgumentException("Matrix has no rectangle shape");
        }
        boolean[] vector = new boolean[matrix.length * matrix[0].length];
        int dimension = matrix[0].length;
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, vector, i * dimension, dimension);
        }
        return vector;
    }

    public static int[] alignMatrixToVector(int[][] matrix) {
        if(!hasRectangleShape(matrix)) {
            throw new IllegalArgumentException("Matrix has no rectangle shape");
        }
        int[] vector = new int[matrix.length * matrix[0].length];
        int dimension = matrix[0].length;
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, vector, i * dimension, dimension);
        }
        return vector;
    }

    public static double[] alignMatrixToVector(double[][] matrix) {
        if(!hasRectangleShape(matrix)) {
            throw new IllegalArgumentException("Matrix has no rectangle shape");
        }
        double[] vector = new double[matrix.length * matrix[0].length];
        int dimension = matrix[0].length;
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, vector, i * dimension, dimension);
        }
        return vector;
    }

    public static double[] concatenateTwoDoubleVectors(double[] first, double[] second) {
        double[] vector = new double[first.length + second.length];
        System.arraycopy(first, 0, vector, 0, first.length);
        System.arraycopy(second, 0, vector, first.length, second.length);
        return vector;
    }

    public static double[] concatenateDoubleAndBooleanVectors(double[] first, boolean[] second) {
        double[] vector = new double[first.length + second.length];
        System.arraycopy(first, 0, vector, 0, first.length);
        for (int i = 0, j = first.length; i < second.length; i++) {
            vector[j] = second[i] ? 1.0 : 0.0;
        }
        return vector;
    }

}
