package com.tugalsan.api.list.client;

import com.tugalsan.api.cast.client.*;
import java.util.*;
import java.util.stream.*;

public class TGS_ListSortUtils {

    public static void sortReversed(List target) {
        Collections.sort(target, Collections.reverseOrder());
    }

    public static void sortLong(List<Long> v) {
        var o = (Long[]) v.toArray();
        Arrays.parallelSort(o);
        v.clear();
        Arrays.stream(o).forEachOrdered(item -> v.add(item));
    }

    public static void sortPrimativeLong(long[] v) {
        Arrays.parallelSort(v);
    }

    public static void sortDouble(List<Double> v) {
        var o = (Double[]) v.toArray();
        Arrays.parallelSort(o);
        v.clear();
        Arrays.stream(o).forEachOrdered(item -> v.add(item));
    }

    public static void sortPrimativeDouble2(double[] sortable, double[] effected) {
        var sorted = true;
        double temp;
        while (sorted) {
            sorted = false;
            for (var i = 0; i < sortable.length - 1; i++) {
                if (sortable[i] > sortable[i + 1]) {
                    temp = sortable[i];
                    sortable[i] = sortable[i + 1];
                    sortable[i + 1] = temp;
                    temp = effected[i];
                    effected[i] = effected[i + 1];
                    effected[i + 1] = temp;
                    sorted = true;
                }
            }
        }
    }

    public static void sortPrimativeDouble(double[] v) {
        Arrays.parallelSort(v);
    }

    public static void sortObject(List v) {
        var ss = new String[v.size()];
        IntStream.range(0, v.size()).forEachOrdered(i -> ss[i] = String.valueOf(v.get(i)));
        Arrays.parallelSort(ss);
        v.clear();
        Arrays.stream(ss).forEachOrdered(item -> v.add(item));
    }

    public static void sortString(List<String> v) {
        sortObject(v);
    }

    public static void sortStringAsDouble(List<String> v) {
        TGS_ListSortUtils.sortList(v, (Comparator<String>) (String a, String b) -> {
            var da = TGS_CastUtils.toDouble(a);
            if (da == null) {
                return 0;
            }
            var db = TGS_CastUtils.toDouble(b);
            if (db == null) {
                return 0;
            }
            return Double.compare(da, db);
        });
    }

    public static void sortStringAsLong(List<String> v) {
        TGS_ListSortUtils.sortList(v, (Comparator<String>) (String a, String b) -> {
            var da = TGS_CastUtils.toLong(a);
            var db = TGS_CastUtils.toLong(b);
            if (da == null || db == null) {
                return 0;
            }
            return Long.compare(da, db);
        });
    }

    public static void sortStringAsInteger(List<String> v) {
        TGS_ListSortUtils.sortList(v, (Comparator<String>) (String a, String b) -> {
            var da = TGS_CastUtils.toInteger(a);
            if (da == null) {
                return 0;
            }
            var db = TGS_CastUtils.toInteger(b);
            if (db == null) {
                return 0;
            }
            return Integer.compare(da, db);
        });
    }

    public static void sortInt(List<Integer> v) {
        var o = (Integer[]) v.toArray();
        Arrays.parallelSort(o);
        v.clear();
        Arrays.stream(o).forEachOrdered(item -> v.add(item));
    }

    public static void sortPrimativeInt(int[] v) {
        Arrays.parallelSort(v);
    }

    public static void sortPrimativeIntReversed(int[] arr) {
        sortPrimativeInt(arr);
        TGS_ListSwapUtils.reverseInt(arr);
    }

    public static void sortList(List target, Comparator c) {
        Collections.sort(target, c);
    }
}
