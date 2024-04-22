package com.tugalsan.api.thread.server;

@Deprecated //JUST USE VIRTUAL THREAD
public class TS_ThreadInfo {

    public static String name() {
        return Thread.currentThread().getThreadGroup().getName();
    }

    public static int count() {
        return Thread.activeCount();
    }
}
