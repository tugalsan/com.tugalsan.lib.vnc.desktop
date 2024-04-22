package com.tugalsan.api.callable.client;

public interface TGS_CallableType4<R, A, B, C, D> {

    public R call(A input0, B input1, C input2, D input3);

    default R Void() {
        return TGS_CallableUtils.Null();
    }
}
