module com.tugalsan.lib.vnc.desktop {
    requires java.logging;
    requires java.datatransfer;
    requires java.desktop;
    requires java.prefs;
    requires com.sun.jna.platform;
    requires com.tugalsan.api.thread;
    requires com.tugalsan.api.runnable;
    requires com.tugalsan.api.charset;
    requires com.tugalsan.api.callable;
    requires com.tugalsan.api.unsafe;
    requires com.tugalsan.api.desktop;
    exports com.tugalsan.lib.vnc.desktop.server;
}
