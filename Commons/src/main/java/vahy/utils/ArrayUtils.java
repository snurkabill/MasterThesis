package vahy.utils;

import java.lang.reflect.Array;
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
        if (!hasRectangleShape(matrix)) {
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
        if (!hasRectangleShape(matrix)) {
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
        if (!hasRectangleShape(matrix)) {
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


    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     * <p>
     * <pre>
     * ArrayUtils.addAll(null, null)     = null
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * ArrayUtils.addAll([null], [null]) = [null, null]
     * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
     * </pre>
     *
     * @param <T>    the component type of the array
     * @param array1 the first array whose elements are added to the new array, may be {@code null}
     * @param array2 the second array whose elements are added to the new array, may be {@code null}
     * @return The new array, {@code null} if both arrays are {@code null}.
     * The type of the new array is the type of the first array,
     * unless the first array is null, in which case the type is the same as the second array.
     * @throws IllegalArgumentException if the array types are incompatible
     * @since 2.1
     */
    public static <T> T[] addAll(final T[] array1, final T... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
        final T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (final ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because:
             * - it would be a wasted check most of the time
             * - safer, in case check turns out to be too strict
             */
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
                    + type1.getName(), ase);
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    // Clone
    //-----------------------------------------------------------------------

    /**
     * <p>Shallow clones an array returning a typecast result and handling
     * {@code null}.
     * <p>
     * <p>The objects in the array are not cloned, thus there is no special
     * handling for multi-dimensional arrays.
     * <p>
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param <T>   the component type of the array
     * @param array the array to shallow clone, may be {@code null}
     * @return the cloned array, {@code null} if {@code null} input
     */
    public static <T> T[] clone(final T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

}
