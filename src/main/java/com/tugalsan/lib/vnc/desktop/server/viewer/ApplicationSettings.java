package com.tugalsan.lib.vnc.desktop.server.viewer;

import java.util.logging.Level;

public class ApplicationSettings {

    Level logLevel;
    public String password;

    public void calculateLogLevel(boolean verbose, boolean verboseMore) {
        logLevel = verboseMore ? Level.FINER : verbose ? Level.FINE : Level.INFO;
    }
}
