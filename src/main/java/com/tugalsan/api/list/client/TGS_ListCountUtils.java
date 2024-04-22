package com.tugalsan.api.list.client;

import java.util.stream.IntStream;

public class TGS_ListCountUtils {

    public static long countTrue(boolean[] filter, boolean countWhat) {
        return IntStream.range(0, filter.length).mapToObj(i -> filter[i]).filter(b -> b == countWhat).count();
    }
}
