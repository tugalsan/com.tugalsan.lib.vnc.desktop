package com.tugalsan.api.callable.client;

import java.util.concurrent.Callable;

public interface TGS_Callable<R> extends Callable<R>{

    public R call();
}
