module com.tugalsan.lib.vnc.desktop {
    requires java.logging;
    requires java.datatransfer;
    requires java.desktop;
    requires java.prefs;
    requires trilead.ssh2;
    requires com.tugalsan.api.thread;
    requires com.tugalsan.api.executable;
    requires com.tugalsan.api.charset;
    requires com.tugalsan.api.compiler;
    requires com.tugalsan.api.unsafe;
    requires com.tugalsan.api.desktop;
    exports com.tugalsan.lib.vnc.desktop.server;
}
