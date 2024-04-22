package com.tugalsan.api.callable.client;

public interface TGS_CallableType3<R, A, B, C> {

    public R call(A input0, B input1, C input2);

    default R Void() {
        return TGS_CallableUtils.Null();
    }
}
