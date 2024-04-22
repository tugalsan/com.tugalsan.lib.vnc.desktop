package com.tugalsan.api.list.client;

import com.tugalsan.api.stream.client.*;
import com.tugalsan.api.tuple.client.TGS_Tuple2;
import com.tugalsan.api.validator.client.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class TGS_ListUtils {

    private TGS_ListUtils() {//TO DETECT OLD CODES

    }

    public void removeIf(List list, TGS_ValidatorType2<List, Object> validator) {
        removeIf(list, (Predicate) (Object t) -> validator.validate(list, t));
    }

    public void removeIf(List list, Predicate filter) {
        if (list == null) {
            return;
        }
        if (list instanceof ArrayList) {//GWT does not like pattern; check on 2.10 version again!
            ((ArrayList) list).removeIf(filter);
            return;
        }
        TGS_StreamUtils.reverse(0, list.size())
                .filter(i -> filter.test(i))
                .forEachOrdered(i -> list.remove(i));
    }

    public static <T> List<T> of(List<T> unmodifiableList) {
        return new ArrayList(unmodifiableList);
    }

    public static <T> List<T> of(T... a) {
        return new ArrayList(Arrays.asList(a));
    }

    public static int[] copy(int[] array) {
        return Arrays.copyOf(array, array.length);
    }

    public static long[] copy(long[] array) {
        return Arrays.copyOf(array, array.length);
    }

    public static double[] copy(double[] array) {
        return Arrays.copyOf(array, array.length);
    }

    public static <T> List<T> toList(T value, int size) {
        List<T> arr = TGS_ListUtils.of();
        IntStream.range(0, size).forEachOrdered(i -> arr.add(value));
        return arr;
    }

    public static List<String> toList(CharSequence[] str) {
        return TGS_StreamUtils.toLst(
                TGS_ListUtils.of(str).stream()
                        .map(c -> String.valueOf(c))
        );
    }

    public static <T> List<T> filterArray(T[] ids, boolean[] filter) {
        List<T> data = TGS_ListUtils.of();
        IntStream.range(0, ids.length).forEachOrdered(i -> {
            if (filter[i]) {
                data.add(ids[i]);
            }
        });
        return data;
    }

    public static void reverse(List v) {
        var o = v.toArray();
        IntStream.range(0, o.length).parallel().forEach(i -> v.set(i, o[o.length - 1 - i]));
    }

    public static void clone(List<List<Integer>> source, List<List<Integer>> target) {
        if (source == null || target == null) {
            return;
        }
        target.clear();
        source.stream().forEachOrdered(rowSrc -> {
            List<Integer> rowTar = TGS_ListUtils.of();
            rowSrc.stream().forEachOrdered(i -> rowTar.add(rowSrc.get(i)));
            target.add(rowTar);
        });
    }

    public static void trimArray(List<List<Integer>> pm, int sizetoBeTrimmed) {
        for (var i = 0; i < pm.size(); i++) {
            while (pm.get(i).size() > sizetoBeTrimmed) {
                pm.get(i).remove(sizetoBeTrimmed);
            }
        }
    }

    public static List<String> toListString(List array, String nullValue) {
        var array_size = array.size();
        List<String> strArray = new ArrayList(array_size);
        IntStream.range(0, array_size).forEachOrdered(i -> {
            var o = array.get(i);
            strArray.add(o == null ? nullValue : o.toString());
        });
        return strArray;
    }

    public static String toString_commaSpace(int[] array) {
        var sb = new StringJoiner(", ");
        Arrays.stream(array).forEachOrdered(i -> sb.add(String.valueOf(i)));
        return sb.toString();
    }

    public static int[] createInt(int start, int steps, int len) {
        var arr = new int[len];
        IntStream.range(0, len).parallel().forEach(i -> arr[i] = start + steps * (i - 1));
        return arr;
    }

    public static int[] createInt(int start, int len) {
        return IntStream.range(start, start + len).toArray();
    }

    public static double[] createDbl(int start, int len) {
        return IntStream.range(start, start + len).mapToDouble(i -> i).toArray();
    }

    public static double[] createDouble(double start, double steps, int len) {
        var arr = new double[len];
        IntStream.range(0, len).parallel().forEach(i -> arr[i] = start + steps * i);
        return arr;
    }

    public static TGS_Tuple2<CharSequence, List<CharSequence>> sliceFirstToken(List<CharSequence> parsedLine) {
        return TGS_Tuple2.of(
                parsedLine.isEmpty() ? null : parsedLine.get(0),
                TGS_StreamUtils.toLst(
                        IntStream.range(0, parsedLine.size())
                                .filter(i -> i != 0)
                                .mapToObj(i -> parsedLine.get(i))
                )
        );
    }
}
