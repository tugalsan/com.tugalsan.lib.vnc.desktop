package com.tugalsan.api.callable.client;

public interface TGS_CallableType5<R, A, B, C, D, E> {

    public R call(A input0, B input1, C input2, D input3, E input4);

    default R Void() {
        return TGS_CallableUtils.Null();
    }
}
