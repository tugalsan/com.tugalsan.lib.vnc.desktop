package com.tugalsan.api.runnable.client;

import com.tugalsan.api.callable.client.*;

public class TGS_RunnableUtils {

    public static <T> TGS_Callable<T> toCallable0(TGS_Runnable runnable) {
        return () -> {
            runnable.run();
            return TGS_CallableUtils.Null();
        };
    }

    public static <T, A> TGS_CallableType1<T, A> toCallable1(TGS_RunnableType1<A> runnable) {
        return w1 -> {
            runnable.run(w1);
            return TGS_CallableUtils.Null();
        };
    }

    public static <T, A, B> TGS_CallableType2<T, A, B> toCallable1(TGS_RunnableType2<A, B> runnable) {
        return (w1, w2) -> {
            runnable.run(w1, w2);
            return TGS_CallableUtils.Null();
        };
    }

    public static <T, A, B, C> TGS_CallableType3<T, A, B, C> toCallable1(TGS_RunnableType3<A, B, C> runnable) {
        return (w1, w2, w3) -> {
            runnable.run(w1, w2, w3);
            return TGS_CallableUtils.Null();
        };
    }

    public static <T, A, B, C, D> TGS_CallableType4<T, A, B, C, D> toCallable1(TGS_RunnableType4<A, B, C, D> runnable) {
        return (w1, w2, w3, w4) -> {
            runnable.run(w1, w2, w3, w4);
            return TGS_CallableUtils.Null();
        };
    }

    public static <T, A, B, C, D, E> TGS_CallableType5<T, A, B, C, D, E> toCallable1(TGS_RunnableType5<A, B, C, D, E> runnable) {
        return (w1, w2, w3, w4, w5) -> {
            runnable.run(w1, w2, w3, w4, w5);
            return TGS_CallableUtils.Null();
        };
    }

    public static TGS_Runnable doNothing0() {
        return () -> {
        };
    }

    public static TGS_RunnableType1 doNothing1() {
        return w1 -> {
        };
    }

    public static TGS_RunnableType2 doNothing2() {
        return (w1, w2) -> {
        };
    }

    public static TGS_RunnableType3 doNothing3() {
        return (w1, w2, w3) -> {
        };
    }

    public static TGS_RunnableType4 doNothing4() {
        return (w1, w2, w3, w4) -> {
        };
    }

}
