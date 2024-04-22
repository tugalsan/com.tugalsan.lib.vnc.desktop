package com.tugalsan.api.list.client;

import com.tugalsan.api.cast.client.*;
import com.tugalsan.api.stream.client.*;
import java.util.*;
import java.util.stream.*;

public class TGS_ListCastUtils {

    public static boolean[] toPrimativeBoolean(List<Boolean> values) {
        var size = values.size();
        boolean[] prim = new boolean[size];
        IntStream.range(0, size).forEachOrdered(i -> prim[i] = values.get(i));
        return prim;
    }

    public static double[] toPrimativeDouble(List<Double> values) {
        var size = values.size();
        double[] prim = new double[size];
        IntStream.range(0, size).forEachOrdered(i -> prim[i] = values.get(i));
        return prim;
    }

    public static float[] toPrimativeFloat(List<Float> values) {
        var size = values.size();
        float[] prim = new float[size];
        IntStream.range(0, size).forEachOrdered(i -> prim[i] = values.get(i));
        return prim;
    }

    public static long[] toPrimativeLong(List<Long> values) {
        var size = values.size();
        long[] prim = new long[size];
        IntStream.range(0, size).forEachOrdered(i -> prim[i] = values.get(i));
        return prim;
    }

    public static int[] toPrimativeInteger(List<Integer> values) {
        var size = values.size();
        int[] prim = new int[size];
        IntStream.range(0, size).forEachOrdered(i -> prim[i] = values.get(i));
        return prim;
    }

    public static List<Long> toLong(long[] primativeUnique) {
        return TGS_StreamUtils.toLst(Arrays.stream(primativeUnique).boxed());
    }

    public static List<Double> toDouble(double[] primativeUnique) {
        return TGS_StreamUtils.toLst(Arrays.stream(primativeUnique).boxed());
    }

    public static List<Long> toLong(List input) {
        if (input == null) {
            return null;
        }
        return TGS_StreamUtils.toLst(
                IntStream.range(0, input.size())
                        .mapToObj(i -> String.valueOf(input.get(i)))
                        .map(str -> TGS_CastUtils.toLong(str))
        );
    }

    public static CharSequence[] toArrayCharSequence(List<CharSequence> input) {
        return input.stream().toArray(CharSequence[]::new);
    }

    public static String[] toArrayString(List<String> input) {
        return input.stream().toArray(String[]::new);
    }

    public static double[] toArrayDouble(List<Double> input) {
        return input.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static int[] toArrayInteger(List<Integer> input) {
        return input.stream().mapToInt(Integer::intValue).toArray();
    }

    public static long[] toArrayLong(List<Long> input) {
        return input.stream().mapToLong(Long::longValue).toArray();
    }
}
