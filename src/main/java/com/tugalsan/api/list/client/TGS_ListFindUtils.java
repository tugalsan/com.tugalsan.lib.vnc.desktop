package com.tugalsan.api.list.client;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class TGS_ListFindUtils {

    public static int findIndex(Integer[] source, Integer key) {
        if (source == null) {
            return -1;
        }
        return IntStream.range(0, source.length).filter(i -> source[i].intValue() == key).findAny().orElse(-1);
    }

    public static int findIndex(CharSequence[] source, CharSequence key) {
        if (source == null || key == null) {
            return -1;
        }
        return IntStream.range(0, source.length).filter(i -> Objects.equals(source[i].toString(), key.toString())).findAny().orElse(-1);
    }

    public static int findIndex(List<String> source, CharSequence key) {
        if (source == null || key == null) {
            return -1;
        }
        return IntStream.range(0, source.size()).filter(i -> Objects.equals(source.get(i), key.toString())).findAny().orElse(-1);
    }

    public static int findIndex(List<Integer> source, Integer key) {
        if (source == null || key == null) {
            return -1;
        }
        return IntStream.range(0, source.size()).filter(i -> source.get(i).intValue() == key).findAny().orElse(-1);
    }

    public static int[] findAllIndexes(CharSequence[] source, CharSequence key, boolean parallel) {
        if (source == null || key == null) {
            return null;
        }
        if (parallel) {
            return IntStream.range(0, source.length).parallel().filter(i -> Objects.equals(source[i].toString(), key.toString())).toArray();
        }
        return IntStream.range(0, source.length).filter(i -> Objects.equals(source[i].toString(), key.toString())).toArray();
    }

}
