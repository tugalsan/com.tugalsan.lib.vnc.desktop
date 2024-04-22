package com.tugalsan.api.list.client;

import java.util.*;
import java.util.stream.*;

public class TGS_ListSwapUtils {

    public static final <T> void swap(int i, int j, T[]... a) {
        if (i == j) {
            return;
        }
        TGS_ListUtils.of(a).parallelStream().forEach(arr -> {
            T t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        });
    }

    public static final void swap(int i, int j, List<?>... l) {//? makes it like read-only
        if (i == j) {
            return;
        }
        TGS_ListUtils.of(l).parallelStream().forEach(lst -> {
            Collections.swap(lst, i, j);
        });
    }

    public static void reverseInt(int... arr) {
        IntStream.range(0, arr.length / 2).forEachOrdered(i -> {
            var tmp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = tmp;
        });
    }

    public static void reverseLng(long... arr) {
        IntStream.range(0, arr.length / 2).forEachOrdered(i -> {
            var tmp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = tmp;
        });
    }

    public static void reverseDbl(double... arr) {
        IntStream.range(0, arr.length / 2).forEachOrdered(i -> {
            var tmp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = tmp;
        });
    }
}
