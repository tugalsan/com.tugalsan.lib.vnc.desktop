module com.tugalsan.lib.vnc.desktop {
    requires java.logging;
    requires java.datatransfer;
    requires java.desktop;
    requires java.prefs;
    requires jna;
    exports com.tugalsan.lib.vnc.desktop.server;
    exports com.tugalsan.api.callable.client;
    exports com.tugalsan.api.runnable.client;
    exports com.tugalsan.api.charset.client;
    exports com.tugalsan.api.desktop.server;
    exports com.tugalsan.api.shape.client;
    exports com.tugalsan.api.thread.server;
    exports com.tugalsan.api.thread.server.sync;
    exports com.tugalsan.api.thread.server.async;
    exports com.tugalsan.api.list.client;
    exports com.tugalsan.api.tuple.client;
    exports com.tugalsan.api.stream.client;
    exports com.tugalsan.api.unsafe.client;
    exports com.tugalsan.api.string.client;
    exports com.tugalsan.api.log.server;
    exports com.tugalsan.api.random.client;
    exports com.tugalsan.api.random.server;
    exports com.tugalsan.api.cast.client;
    exports com.tugalsan.api.time.client;
}
