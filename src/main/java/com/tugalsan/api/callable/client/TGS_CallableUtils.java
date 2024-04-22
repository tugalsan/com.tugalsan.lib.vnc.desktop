package com.tugalsan.api.callable.client;

public class TGS_CallableUtils {

    public static <T> T Null() {
        return (T) null;
    }

    public static TGS_Callable doNothing0() {
        return () -> Null();
    }

    public static TGS_CallableType1 doNothing1() {
        return w1 -> Null();
    }

    public static TGS_CallableType2 doNothing2() {
        return (w1, w2) -> Null();
    }

    public static TGS_CallableType3 doNothing3() {
        return (w1, w2, w3) -> Null();
    }

    public static TGS_CallableType4 doNothing4() {
        return (w1, w2, w3, w4) -> Null();
    }

    public static TGS_CallableType5 doNothing5() {
        return (w1, w2, w3, w4, w5) -> Null();
    }
}
