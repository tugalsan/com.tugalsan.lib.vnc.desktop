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
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_Type;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_LocalPointer;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Settings;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_TunnelType;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopUtils_Strings;

import java.util.logging.Logger;

public class TS_LibVncDesktopViewer_ParametersHandler {

    public static final String ARG_LOCAL_POINTER = "LocalPointer";
    public static final String ARG_SCALING_FACTOR = "ScalingFactor";
    public static final String ARG_COLOR_DEPTH = "ColorDepth";
    public static final String ARG_JPEG_IMAGE_QUALITY = "JpegImageQuality";
    public static final String ARG_COMPRESSION_LEVEL = "CompressionLevel";
    public static final String ARG_ENCODING = "Encoding";
    public static final String ARG_SHARE_DESKTOP = "ShareDesktop";
    public static final String ARG_ALLOW_COPY_RECT = "AllowCopyRect";
    public static final String ARG_VIEW_ONLY = "ViewOnly";
    public static final String ARG_SHOW_CONTROLS = "ShowControls";
    public static final String ARG_OPEN_NEW_WINDOW = "OpenNewWindow";
    public static final String ARG_PASSWORD = "password";
    public static final String ARG_PORT = "port";
    public static final String ARG_HOST = "host";
    public static final String ARG_VERBOSE = "v";
    public static final String ARG_VERBOSE_MORE = "vv";
    public static final String ARG_CONVERT_TO_ASCII = "ConvertToASCII";
    public static final String ARG_ALLOW_CLIPBOARD_TRANSFER = "AllowClipboardTransfer";
    public static final String ARG_REMOTE_CHARSET = "RemoteCharset";
    public static final String ARG_SHOW_CONNECTION_DIALOG = "showConnectionDialog";

    public static void completeParserOptions(TS_LibVncDesktopViewer_CliParser parser) {
        parser.addOption(ARG_HOST, "", "Server host name.");
        parser.addOption(ARG_PORT, "0", "Port number.");
        parser.addOption(ARG_PASSWORD, null, "Password to the server.");
        parser.addOption(ARG_SHOW_CONTROLS, null, "Set to \"No\" if you want to get rid of that "
                + "button panel at the top. Default: \"Yes\".");
        parser.addOption(ARG_SHOW_CONNECTION_DIALOG, null, "Set to \"No\" if you want not to show initial connection dialog."
                + " Default: \"Yes\".");
        parser.addOption(ARG_VIEW_ONLY, null, "When set to \"Yes\", then all keyboard and mouse "
                + "events in the desktop window will be silently ignored and will not be passed "
                + "to the remote side. Default: \"No\".");
        parser.addOption(ARG_ALLOW_CLIPBOARD_TRANSFER, null, "When set to \"Yes\", transfer of clipboard contents is allowed. "
                + "Default: \"Yes\".");
        parser.addOption(ARG_REMOTE_CHARSET, null, "Charset encoding is used on remote system. Use this option to specify character encoding will be used for encoding clipboard text content to. Default value: local system default character encoding. Set the value to 'standard' for using 'Latin-1' charset which is only specified by rfb standard for clipboard transfers.");
        parser.addOption(ARG_SHARE_DESKTOP, null, "Share the connection with other clients "
                + "on the same VNC server. The exact behaviour in each case depends on the server "
                + "configuration. Default: \"Yes\".");
        parser.addOption(ARG_ALLOW_COPY_RECT, null, "The \"CopyRect\" encoding saves bandwidth "
                + "and drawing time when parts of the remote screen are moving around. "
                + "Most likely, you don't want to change this setting. Default: \"Yes\".");
        parser.addOption(ARG_ENCODING, null, "The preferred encoding. Possible values: \"Tight\", "
                + "\"Hextile\", \"ZRLE\", and \"Raw\". Default: \"Tight\".");
        parser.addOption(ARG_COMPRESSION_LEVEL, null, "Use specified compression level for "
                + "\"Tight\" and \"Zlib\" encodings. Values: 1-9. Level 1 uses minimum of CPU "
                + "time on the server but achieves weak compression ratios. Level 9 offers best "
                + "compression but may be slow.");
        //noinspection ConstantConditions
        parser.addOption(ARG_JPEG_IMAGE_QUALITY, null, "Use the specified image quality level "
                + "in \"Tight\" encoding. Values: 1-9, Lossless. Default value: "
                + (TS_LibVncDesktopRfbProtocol_Settings.DEFAULT_JPEG_QUALITY > 0
                        ? String.valueOf(TS_LibVncDesktopRfbProtocol_Settings.DEFAULT_JPEG_QUALITY)
                        : "\"Lossless\"")
                + ". To prevent server of using "
                + "lossy JPEG compression in \"Tight\" encoding, use \"Lossless\" value here.");
        parser.addOption(ARG_LOCAL_POINTER, null, "Possible values: on/yes/true (draw pointer locally), off/no/false (let server draw pointer), hide). "
                + "Default: \"On\".");
        parser.addOption(ARG_CONVERT_TO_ASCII, null, "Whether to convert keyboard input to ASCII ignoring locale. Possible values: yes/true, no/false). "
                + "Default: \"No\".");
        parser.addOption(ARG_COLOR_DEPTH, null, "Bits per pixel color format. Possible values: 3 (for 8 colors), 6 (64 colors), 8 (256 colors), 16 (65 536 colors), 24 (16 777 216 colors), 32 (same as 24).");
        parser.addOption(ARG_SCALING_FACTOR, null, "Scale local representation of the remote desktop on startup. "
                + "The value is interpreted as scaling factor in percents. The default value of 100% "
                + "corresponds to the original framebuffer size.");
        parser.addOption(ARG_VERBOSE, null, "Verbose console output.");
        parser.addOption(ARG_VERBOSE_MORE, null, "More verbose console output.");

    }

    static int completeSettingsFromCLI(final TS_LibVncDesktopViewer_CliParser parser, TS_LibVncDesktopViewer_SettingsViewerConnectionParams connectionParams, TS_LibVncDesktopRfbProtocol_Settings rfbSettings, TS_LibVncDesktopViewer_SettingsUi uiSettings, TS_LibVncDesktopViewer_ApplicationSettings applicationSettings) {
        var mask = completeSettings((String name) -> {
            if (TGS_CharSetCast.current().equalsIgnoreCase(ARG_VERBOSE, name) || TGS_CharSetCast.current().equalsIgnoreCase(ARG_VERBOSE_MORE, name)) {
                return parser.isSet(name) ? name : null;
            }
            return parser.getValueFor(name);
        },
                connectionParams, rfbSettings, uiSettings, applicationSettings);
        // when hostName == a.b.c.d:3 where :3 is display num (X Window) we need add display num to port number
        if (!TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(connectionParams.hostName)) {
            splitConnectionParams(connectionParams, connectionParams.hostName);
        }
        if (parser.isSetPlainOptions()) {
            splitConnectionParams(connectionParams, parser.getPlainOptionAt(0));
            if (parser.getPlainOptionsNumber() > 1) {
                try {
                    connectionParams.setPortNumber(parser.getPlainOptionAt(1));
                } catch (TS_LibVncDesktopViewer_SettingsWrongParameterException e) {
                    //nop
                }
            }
        }
        return mask;
    }

    /**
     * Split host string into hostName + port number and set ConnectionParans.
     * a.b.c.d:5000 -> hostName == a.b.c.d, portNumber == 5000 a.b.c.d::5000 ->
     * hostName == a.b.c.d, portNumber == 5000
     */
    private static void splitConnectionParams(final TS_LibVncDesktopViewer_SettingsViewerConnectionParams connectionParams, String host) {
        var indexOfColon = host.indexOf(':');
        if (indexOfColon > 0) {
            var splitted = host.split(":");
            connectionParams.hostName = splitted[0];
            if (splitted.length > 1) {
                try {
                    connectionParams.setPortNumber(splitted[splitted.length - 1]);
                } catch (TS_LibVncDesktopViewer_SettingsWrongParameterException e) {
                    //nop
                }
            }
        } else {
            connectionParams.hostName = host;
        }
    }

    private interface ParamsRetriever {

        String getParamByName(String name);
    }

    private static int completeSettings(ParamsRetriever pr, TS_LibVncDesktopViewer_SettingsViewerConnectionParams connectionParams, TS_LibVncDesktopRfbProtocol_Settings rfbSettings, TS_LibVncDesktopViewer_SettingsUi uiSettings, TS_LibVncDesktopViewer_ApplicationSettings applicationSettings) {
        completeConnectionSettings(pr, connectionParams);
        completeApplicationSettings(pr, applicationSettings);
        var uiMask = completeUiSettings(pr, uiSettings);
        var rfbMask = completeRfbSettings(pr, rfbSettings);
        return (uiMask << 16) | rfbMask;
    }

    private static int completeRfbSettings(ParamsRetriever pr, TS_LibVncDesktopRfbProtocol_Settings rfbSettings) {
        var viewOnlyParam = pr.getParamByName(ARG_VIEW_ONLY);
        var allowClipboardTransfer = pr.getParamByName(ARG_ALLOW_CLIPBOARD_TRANSFER);
        var remoteCharsetName = pr.getParamByName(ARG_REMOTE_CHARSET);
        var allowCopyRectParam = pr.getParamByName(ARG_ALLOW_COPY_RECT);
        var shareDesktopParam = pr.getParamByName(ARG_SHARE_DESKTOP);
        var encodingParam = pr.getParamByName(ARG_ENCODING);
        var compressionLevelParam = pr.getParamByName(ARG_COMPRESSION_LEVEL);
        var jpegQualityParam = pr.getParamByName(ARG_JPEG_IMAGE_QUALITY);
        var colorDepthParam = pr.getParamByName(ARG_COLOR_DEPTH);
        var localPointerParam = pr.getParamByName(ARG_LOCAL_POINTER);
        var convertToAsciiParam = pr.getParamByName(ARG_CONVERT_TO_ASCII);

        var rfbMask = 0;
        rfbSettings.setViewOnly(parseBooleanOrDefault(viewOnlyParam, false));
        if (isGiven(viewOnlyParam)) {
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_VIEW_ONLY;
        }
        rfbSettings.setAllowClipboardTransfer(parseBooleanOrDefault(allowClipboardTransfer, true));
        if (isGiven(allowClipboardTransfer)) {
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_ALLOW_CLIPBOARD_TRANSFER;
        }
        rfbSettings.setRemoteCharsetName(remoteCharsetName);
        rfbSettings.setAllowCopyRect(parseBooleanOrDefault(allowCopyRectParam, true));
        if (isGiven(allowCopyRectParam)) {
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_ALLOW_COPY_RECT;
        }
        rfbSettings.setSharedFlag(parseBooleanOrDefault(shareDesktopParam, true));
        if (isGiven(shareDesktopParam)) {
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_SHARED;
        }
        rfbSettings.setConvertToAscii(parseBooleanOrDefault(convertToAsciiParam, false));
        if (isGiven(convertToAsciiParam)) {
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_CONVERT_TO_ASCII;
        }
        if (TGS_CharSetCast.current().equalsIgnoreCase(TS_LibVncDesktopRfbEncoding_Type.TIGHT.getName(), encodingParam)) {
            rfbSettings.setPreferredEncoding(TS_LibVncDesktopRfbEncoding_Type.TIGHT);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_ENCODINGS;
        }
        if (TGS_CharSetCast.current().equalsIgnoreCase(TS_LibVncDesktopRfbEncoding_Type.HEXTILE.getName(), encodingParam)) {
            rfbSettings.setPreferredEncoding(TS_LibVncDesktopRfbEncoding_Type.HEXTILE);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_ENCODINGS;
        }
        if (TGS_CharSetCast.current().equalsIgnoreCase(TS_LibVncDesktopRfbEncoding_Type.ZRLE.getName(), encodingParam)) {
            rfbSettings.setPreferredEncoding(TS_LibVncDesktopRfbEncoding_Type.ZRLE);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_ENCODINGS;
        }
        if (TGS_CharSetCast.current().equalsIgnoreCase(TS_LibVncDesktopRfbEncoding_Type.RAW_ENCODING.getName(), encodingParam)) {
            rfbSettings.setPreferredEncoding(TS_LibVncDesktopRfbEncoding_Type.RAW_ENCODING);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_ENCODINGS;
        }
        try {
            var compLevel = Integer.parseInt(compressionLevelParam);
            if (rfbSettings.setCompressionLevel(compLevel) == compLevel) {
                rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_COMPRESSION_LEVEL;
            }
        } catch (NumberFormatException e) {
            /* nop */ }
        try {
            var jpegQuality = Integer.parseInt(jpegQualityParam);
            if (jpegQuality > 0 && jpegQuality <= 9) {
                rfbSettings.setJpegQuality(jpegQuality);
                rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_JPEG_QUALITY;
            }
        } catch (NumberFormatException e) {
            if (TGS_CharSetCast.current().equalsIgnoreCase("lossless", jpegQualityParam)) {
                rfbSettings.setJpegQuality(-Math.abs(rfbSettings.getJpegQuality()));
            }
        }
        try {
            var colorDepth = Integer.parseInt(colorDepthParam);
            rfbSettings.setColorDepth(colorDepth);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_COLOR_DEPTH;
        } catch (NumberFormatException e) {
            /* nop */ }

        if (TGS_CharSetCast.current().equalsIgnoreCase("on", localPointerParam) || TGS_CharSetCast.current().equalsIgnoreCase("true", localPointerParam) || TGS_CharSetCast.current().equalsIgnoreCase("yes", localPointerParam)) {
            rfbSettings.setMouseCursorTrack(TS_LibVncDesktopRfbProtocol_LocalPointer.ON);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_MOUSE_CURSOR_TRACK;
        }
        if (TGS_CharSetCast.current().equalsIgnoreCase("off", localPointerParam) || TGS_CharSetCast.current().equalsIgnoreCase("no", localPointerParam) || TGS_CharSetCast.current().equalsIgnoreCase("false", localPointerParam)) {
            rfbSettings.setMouseCursorTrack(TS_LibVncDesktopRfbProtocol_LocalPointer.OFF);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_MOUSE_CURSOR_TRACK;
        }
        if (TGS_CharSetCast.current().equalsIgnoreCase("hide", localPointerParam) || TGS_CharSetCast.current().equalsIgnoreCase("hidden", localPointerParam)) {
            rfbSettings.setMouseCursorTrack(TS_LibVncDesktopRfbProtocol_LocalPointer.HIDE);
            rfbMask |= TS_LibVncDesktopRfbProtocol_Settings.CHANGED_MOUSE_CURSOR_TRACK;
        }
        rfbSettings.setTunnelType(TS_LibVncDesktopRfbProtocol_TunnelType.NOTUNNEL);
        return rfbMask;
    }

    private static int completeUiSettings(ParamsRetriever pr, TS_LibVncDesktopViewer_SettingsUi uiSettings) {
        var uiMask = 0;
        var scaleFactorParam = pr.getParamByName(ARG_SCALING_FACTOR);
        uiSettings.showControls = parseBooleanOrDefault(pr.getParamByName(ARG_SHOW_CONTROLS), true);
        uiSettings.showConnectionDialog = parseBooleanOrDefault(pr.getParamByName(ARG_SHOW_CONNECTION_DIALOG), true);
        if (scaleFactorParam != null) {
            try {
                var scaleFactor = Integer.parseInt(scaleFactorParam.replaceAll("\\D", ""));
                if (scaleFactor >= 10 && scaleFactor <= 200) {
                    uiSettings.setScalePercent(scaleFactor);
                    uiMask |= TS_LibVncDesktopViewer_SettingsUi.CHANGED_SCALE_FACTOR;
                }
            } catch (NumberFormatException e) {
                /* nop */ }
        }
        return uiMask;
    }

    private static void completeApplicationSettings(ParamsRetriever pr, TS_LibVncDesktopViewer_ApplicationSettings applicationSettings) {
        applicationSettings.password = pr.getParamByName(ARG_PASSWORD);
        applicationSettings.calculateLogLevel(!TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(pr.getParamByName(ARG_VERBOSE)),
                !TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(pr.getParamByName(ARG_VERBOSE_MORE)));
    }

    private static void completeConnectionSettings(ParamsRetriever pr, TS_LibVncDesktopViewer_SettingsViewerConnectionParams connectionParams) {
        connectionParams.hostName = pr.getParamByName(ARG_HOST);
        try {
            connectionParams.setPortNumber(pr.getParamByName(ARG_PORT));
        } catch (TS_LibVncDesktopViewer_SettingsWrongParameterException e) {
            Logger.getLogger(TS_LibVncDesktopViewer_ParametersHandler.class.getName()).warning(e.getMessage());
        }
    }

    private static boolean isGiven(String param) {
        return !TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(param);
    }

    private static boolean parseBooleanOrDefault(String param, boolean defaultValue) {
        return defaultValue
                ? !(TGS_CharSetCast.current().equalsIgnoreCase("no", param) || TGS_CharSetCast.current().equalsIgnoreCase("false", param))
                : TGS_CharSetCast.current().equalsIgnoreCase("yes", param) || TGS_CharSetCast.current().equalsIgnoreCase("true", param);
    }

}
