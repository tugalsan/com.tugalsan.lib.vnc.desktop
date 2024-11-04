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

import com.tugalsan.api.charset.client.TGS_CharSet;
import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Settings;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopUtils_Strings;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_ParametersHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command line interface parameters parser
 */
public class TS_LibVncDesktopViewer_CliParser {

    public TS_LibVncDesktopViewer_CliParser(String vncHost, Integer vncPort_orNull, String vncPassword, boolean sshEnable, String sshHost, Integer sshPort_orNull, String sshUser, boolean viewOnly, Integer compLevel_from_1_to_9_orNull_def6, Integer imgQuality_from_1_to_9_orNull_def6, Integer bitPerPixel_3_6_8_16_24_32_or_null_defServer) {
        TS_LibVncDesktopViewer_ParametersHandler.completeParserOptions(this);
        {//SERVER
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_HOST, vncHost, "Server host name.");
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_PORT, String.valueOf((vncPort_orNull == null ? TS_LibVncDesktopViewer_SettingsViewerConnectionParams.DEFAULT_RFB_PORT : vncPort_orNull)), "Port number.");
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_PASSWORD, vncPassword, "Password to the server.");
        }
        {//EXTRA
//                            addOption(ParametersHandler.ARG_REMOTE_CHARSET, null, "Charset encoding is used on remote system. Use this option to specify character encoding will be used for encoding clipboard text content to. Default value: local system default character encoding. Set the value to 'standard' for using 'Latin-1' charset which is only specified by rfb standard for clipboard transfers.");
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_VIEW_ONLY, viewOnly ? "Yes" : null, "When set to \"Yes\", then all keyboard and mouse "
                    + "events in the desktop window will be silently ignored and will not be passed "
                    + "to the remote side. Default: \"No\".");
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_COMPRESSION_LEVEL, compLevel_from_1_to_9_orNull_def6 == null ? null : compLevel_from_1_to_9_orNull_def6.toString(), "Use specified compression level for "
                    + "\"Tight\" and \"Zlib\" encodings. Values: 1-9. Level 1 uses minimum of CPU "
                    + "time on the server but achieves weak compression ratios. Level 9 offers best "
                    + "compression but may be slow.");
            //noinspection ConstantConditions
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_JPEG_IMAGE_QUALITY, imgQuality_from_1_to_9_orNull_def6 == null ? null : imgQuality_from_1_to_9_orNull_def6.toString(), "Use the specified image quality level "
                    + "in \"Tight\" encoding. Values: 1-9, Lossless. Default value: "
                    + (TS_LibVncDesktopRfbProtocol_Settings.DEFAULT_JPEG_QUALITY > 0
                            ? String.valueOf(TS_LibVncDesktopRfbProtocol_Settings.DEFAULT_JPEG_QUALITY)
                            : "\"Lossless\"")
                    + ". To prevent server of using "
                    + "lossy JPEG compression in \"Tight\" encoding, use \"Lossless\" value here.");
            addOption(TS_LibVncDesktopViewer_ParametersHandler.ARG_COLOR_DEPTH, bitPerPixel_3_6_8_16_24_32_or_null_defServer == null ? null : bitPerPixel_3_6_8_16_24_32_or_null_defServer.toString(), "Bits per pixel color format. Possible values: 3 (for 8 colors), 6 (64 colors), 8 (256 colors), 16 (65 536 colors), 24 (16 777 216 colors), 32 (same as 24).");
        }
    }

    private final Map<String, Option> options = new LinkedHashMap();
    private final List<String> plainOptions = new ArrayList();
    private boolean isSetPlainOptions = false;

    final public void addOption(String opName, String defaultValue, String desc) {
        var op = new Option(opName, defaultValue, desc);
        options.put(TGS_CharSetCast.current().toLowerCase(opName), op);
    }

    public void parse(String[] args) {
        for (var p : args) {
            if (p.startsWith("-")) {
                var skipMinuses = p.startsWith("--") ? 2 : 1;
                var params = p.split("=", 2);
                var op = options.get(TGS_CharSetCast.current().toLowerCase(params[0]).substring(skipMinuses));
                if (op != null) {
                    op.isSet = true;
                    if (params.length > 1 && !TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(params[1])) {
                        op.value = params[1];
                    }
                }
            } else if (!p.startsWith("-")) {
                isSetPlainOptions = true;
                plainOptions.add(p);
            }
        }
    }

    public String getValueFor(String param) {
        var op = options.get(TGS_CharSetCast.current().toLowerCase(param));
        return op != null ? op.value : null;
    }

    public boolean isSet(String param) {
        var op = options.get(TGS_CharSetCast.current().toLowerCase(param));
        return op != null && op.isSet;
    }

    public boolean isSetPlainOptions() {
        return isSetPlainOptions;
    }

    public String getPlainOptionAt(int index) {
        return plainOptions.get(index);
    }

    public int getPlainOptionsNumber() {
        return plainOptions.size();
    }

    /**
     * Command line interface option
     */
    private static class Option {

        String opName;
        String desc;
        String value;
        boolean isSet = false;

        Option(String opName, String defaultValue, String desc) {
            this.opName = opName;
            this.desc = desc;
            this.value = defaultValue;
        }
    }

    public String optionsUsage() {
        var sb = new StringBuilder();
        var maxNameLength = 0;
        for (var op : options.values()) {
            if (op.desc.isEmpty()) {
                continue;
            }
            maxNameLength = Math.max(maxNameLength, op.opName.length());
        }
        for (var op : options.values()) {
            if (op.desc.isEmpty()) {
                continue;
            }
            sb.append(" -").append(op.opName);
            for (var i = 0; i < maxNameLength - op.opName.length(); ++i) {
                sb.append(' ');
            }
            sb.append(" : ").append(op.desc).append('\n');
        }
        return sb.toString();
    }
}
