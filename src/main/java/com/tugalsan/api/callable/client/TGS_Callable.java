package com.tugalsan.api.callable.client;

import java.util.concurrent.Callable;

public interface TGS_Callable<R> extends Callable<R> {

    @Override
    public R call();

    default R Void() {
        return TGS_CallableUtils.Null();
    }
}
