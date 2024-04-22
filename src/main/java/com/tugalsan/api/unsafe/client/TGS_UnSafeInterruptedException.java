package com.tugalsan.api.unsafe.client;

public class TGS_UnSafeInterruptedException extends RuntimeException {

    public TGS_UnSafeInterruptedException(InterruptedException e) {
        super(e);
    }

}
