package com.tugalsan.api.callable.client;

public interface TGS_CallableType1<R, A> {

    public R call(A input0);

    default R Void() {
        return TGS_CallableUtils.Null();
    }
}
