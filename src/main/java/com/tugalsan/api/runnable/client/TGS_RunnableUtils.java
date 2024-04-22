package com.tugalsan.api.runnable.client;

public class TGS_RunnableUtils {

    public static TGS_RunnableType1 doNothing1() {
        TGS_RunnableType1 NOOP = w1 -> {
        };
        return NOOP;
    }

    public static TGS_RunnableType2 doNothing2() {
        TGS_RunnableType2 NOOP = (w1, w2) -> {
        };
        return NOOP;
    }

    public static TGS_RunnableType3 doNothing3() {
        TGS_RunnableType3 NOOP = (w1, w2, w3) -> {
        };
        return NOOP;
    }

    public static TGS_RunnableType4 doNothing4() {
        TGS_RunnableType4 NOOP = (w1, w2, w3, w4) -> {
        };
        return NOOP;
    }

}
