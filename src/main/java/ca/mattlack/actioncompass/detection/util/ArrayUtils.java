package ca.mattlack.actioncompass.detection.util;

import java.util.Arrays;
import java.util.function.IntFunction;

public class ArrayUtils {
    public static String toString(int[][][] arr) {
        StringBuilder str = new StringBuilder("[");
        for (int[][] i : arr) {
            str.append(toString(i)).append(",");
        }
        str.append("]");
        return str.toString();
    }

    public static String toString(int[][] arr) {
        StringBuilder str = new StringBuilder("[");
        for (int[] i : arr) {
            str.append(toString(i)).append(",");
        }
        str.append("]");
        return str.toString();
    }

    public static String toString(int[] arr) {
        StringBuilder str = new StringBuilder("[");
        for (int i : arr) {
            str.append(i).append(",");
        }
        str.append("]");
        return str.toString();
    }

    public static Object[] arrayToObject(int[][][] in) {
        return Arrays.stream(in).toArray();
    }

    public static <I> Object[] arrayToObject(I[] in) {
        return Arrays.stream(in).toArray();
    }

    public static <T> T[] castArray(Object[] in, Class<T> clazz, IntFunction<T[]> arrSupplier) {
        return Arrays.stream(in)
                .map(clazz::cast)
                .toArray(arrSupplier);
    }

    public static int[][][] castArrayToTripleInt(Object[] in) {
        return Arrays.stream(in)
                .map(obj -> castArrayToDoubleInt((Object[]) obj))
                .toArray(int[][][]::new);
    }

    public static int[][] castArrayToDoubleInt(Object[] in) {
        return Arrays.stream(in)
                .map(int[].class::cast)
                .toArray(int[][]::new);
    }
}
