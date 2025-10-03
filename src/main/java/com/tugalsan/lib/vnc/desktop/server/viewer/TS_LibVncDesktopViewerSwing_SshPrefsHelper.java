// Copyright (C) 2010 - 2014 GlavSoft LLC.
// All rights reserved.
//
// -----------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// -----------------------------------------------------------------------
//
package com.tugalsan.lib.vnc.desktop.server.viewer;

import module java.logging;
import java.io.*;
import java.util.prefs.*;

public class TS_LibVncDesktopViewerSwing_SshPrefsHelper {

    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopViewerSwing_SshPrefsHelper.class.getName());

    static void clearNode(Preferences node) {
        try { // clear wrong data
            logger.finer("Clear wrong data from preferences node %s".formatted(node.name()));
            node.clear();
            node.sync();
        } catch (BackingStoreException e) {
            logger.warning("Cannot clear/sync preferences node '%s': %s".formatted(node.name(), e.getMessage()));
        }
    }

    static void addRecordTo(Preferences node, String key, String record) throws IOException {
        var out = getStringFrom(node, key);
        if (out.length() > 0 && !out.endsWith("\n")) {
            out += ('\n');
        }
        out += record + '\n';
        clearNode(node);
        update(node, key, out);
    }

    private static void update(Preferences node, String key, String value) {
        var length = value.length();
        if (length <= Preferences.MAX_VALUE_LENGTH) {
            node.put(key, value);
        } else {
            for (int idx = 0, cnt = 1; idx < length; ++cnt) {
                if ((length - idx) > Preferences.MAX_VALUE_LENGTH) {
                    node.put(key + "." + cnt, value.substring(idx, idx + Preferences.MAX_VALUE_LENGTH));
                    idx += Preferences.MAX_VALUE_LENGTH;
                } else {
                    node.put(key + "." + cnt, value.substring(idx));
                    idx = length;
                }
            }
        }
        try {
            node.sync();
        } catch (BackingStoreException e) {
            logger.warning("Cannot sync preferences node '%s': %s".formatted(node.name(), e.getMessage()));
        }
    }

    static String getStringFrom(Preferences sshNode, String key) {
        var out = new StringBuilder();
//        try {
            var str = sshNode.get(key, "");
            out.append(str);
            for (var cnt = 1;; ++cnt) {
                var partKey = key + "." + cnt;
                var part = sshNode.get(partKey, "");
                if (part.length() > 0) {
                    out.append(part);
                } else {
                    break;
                }
            }
//        } catch (Exception r) {
//            TGS_FuncUtils.throwIfInterruptedException(r);
//            logger.warning("Wrong data at '%s#%s' prefs: %s".formatted(sshNode.absolutePath(), key, r.getMessage()));
//            clearNode(sshNode);
//        }
        logger.finer("KnownHosts: \n%s".formatted(out.toString()));
        return out.toString();
    }
}
