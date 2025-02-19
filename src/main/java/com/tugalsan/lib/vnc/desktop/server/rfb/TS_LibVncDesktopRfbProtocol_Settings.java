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
package com.tugalsan.lib.vnc.desktop.server.rfb;

import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopCore_SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_Type;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfb_IChangeSettingsListener;

/**
 * Protocol Settings class
 */
public class TS_LibVncDesktopRfbProtocol_Settings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final TS_LibVncDesktopRfbEncoding_Type DEFAULT_PREFERRED_ENCODING = TS_LibVncDesktopRfbEncoding_Type.TIGHT;
    public static final int DEFAULT_JPEG_QUALITY = 6;
    private static final int DEFAULT_COMPRESSION_LEVEL = -6;

    // color depth constants
    public static final int COLOR_DEPTH_32 = 32;
    public static final int COLOR_DEPTH_24 = 24;
    public static final int COLOR_DEPTH_16 = 16;
    public static final int COLOR_DEPTH_8 = 8;
    public static final int COLOR_DEPTH_6 = 6;
    public static final int COLOR_DEPTH_3 = 3;

    public static final int COLOR_DEPTH_SERVER_SETTINGS = 0;

    private static final int DEFAULT_COLOR_DEPTH = COLOR_DEPTH_24;

    public static final int CHANGED_VIEW_ONLY = 1; // 1 << 0;
    public static final int CHANGED_ENCODINGS = 1 << 1;
    public static final int CHANGED_ALLOW_COPY_RECT = 1 << 2;
    public static final int CHANGED_SHOW_REMOTE_CURSOR = 1 << 3;
    public static final int CHANGED_MOUSE_CURSOR_TRACK = 1 << 4;
    public static final int CHANGED_COMPRESSION_LEVEL = 1 << 5;
    public static final int CHANGED_JPEG_QUALITY = 1 << 6;
    public static final int CHANGED_ALLOW_CLIPBOARD_TRANSFER = 1 << 7;
    public static final int CHANGED_CONVERT_TO_ASCII = 1 << 8;
    public static final int CHANGED_COLOR_DEPTH = 1 << 9;
    public static final int CHANGED_SHARED = 1 << 10;

    private static final int MIN_COMPRESSION_LEVEL = 1;
    private static final int MAX_COMPRESSION_LEVEL = 9;
    private static final int MIN_JPEG_QUALITY = 1;
    private static final int MAX_JPEG_QUALITY = 9;

    private transient int changedSettingsMask;

    private boolean sharedFlag;
    private boolean viewOnly;
    private TS_LibVncDesktopRfbEncoding_Type preferredEncoding;
    private boolean allowCopyRect;
    private boolean showRemoteCursor;
    private TS_LibVncDesktopRfbProtocol_LocalPointer mouseCursorTrack;
    private int compressionLevel;
    private int jpegQuality;
    private boolean allowClipboardTransfer;
    private boolean convertToAscii;
    private int colorDepth;

    private transient final List<TS_LibVncDesktopRfb_IChangeSettingsListener> listeners;
    private transient String remoteCharsetName;
    private TS_LibVncDesktopRfbProtocol_TunnelType tunnelType;

    public static TS_LibVncDesktopRfbProtocol_Settings getDefaultSettings() {
        return new TS_LibVncDesktopRfbProtocol_Settings();
    }

    private TS_LibVncDesktopRfbProtocol_Settings() {
        sharedFlag = true;
        viewOnly = false;
        showRemoteCursor = true;
        mouseCursorTrack = TS_LibVncDesktopRfbProtocol_LocalPointer.ON;
        preferredEncoding = DEFAULT_PREFERRED_ENCODING;
        allowCopyRect = true;
        compressionLevel = DEFAULT_COMPRESSION_LEVEL;
        jpegQuality = DEFAULT_JPEG_QUALITY;
        convertToAscii = false;
        allowClipboardTransfer = true;
        colorDepth = COLOR_DEPTH_SERVER_SETTINGS;

        listeners = new CopyOnWriteArrayList();
        changedSettingsMask = 0;
    }

    public TS_LibVncDesktopRfbProtocol_Settings(TS_LibVncDesktopRfbProtocol_Settings s) {
        this();
        copyDataFrom(s);
        changedSettingsMask = s.changedSettingsMask;
    }

    final public void copyDataFrom(TS_LibVncDesktopRfbProtocol_Settings s) {
        copyDataFrom(s, 0);
    }

    public void copyDataFrom(TS_LibVncDesktopRfbProtocol_Settings s, int mask) {
        if (null == s) {
            return;
        }
        if ((mask & CHANGED_SHARED) == 0) {
            setSharedFlag(s.sharedFlag);
        }
        if ((mask & CHANGED_VIEW_ONLY) == 0) {
            setViewOnly(s.viewOnly);
        }
        if ((mask & CHANGED_ALLOW_COPY_RECT) == 0) {
            setAllowCopyRect(s.allowCopyRect);
        }
        if ((mask & CHANGED_SHOW_REMOTE_CURSOR) == 0) {
            setShowRemoteCursor(s.showRemoteCursor);
        }
        if ((mask & CHANGED_ALLOW_CLIPBOARD_TRANSFER) == 0) {
            setAllowClipboardTransfer(s.allowClipboardTransfer);
        }

        if ((mask & CHANGED_MOUSE_CURSOR_TRACK) == 0) {
            setMouseCursorTrack(s.mouseCursorTrack);
        }
        if ((mask & CHANGED_COMPRESSION_LEVEL) == 0) {
            setCompressionLevel(s.compressionLevel);
        }
        if ((mask & CHANGED_JPEG_QUALITY) == 0) {
            setJpegQuality(s.jpegQuality);
        }
        if ((mask & CHANGED_CONVERT_TO_ASCII) == 0) {
            setConvertToAscii(s.convertToAscii);
        }
        if ((mask & CHANGED_COLOR_DEPTH) == 0) {
            setColorDepth(s.colorDepth);
        }
        if ((mask & CHANGED_ENCODINGS) == 0) {
            setPreferredEncoding(s.preferredEncoding);
        }
    }

    public void addListener(TS_LibVncDesktopRfb_IChangeSettingsListener listener) {
        listeners.add(listener);
    }

    public byte getSharedFlag() {
        return (byte) (sharedFlag ? 1 : 0);
    }

    public boolean isShared() {
        return sharedFlag;
    }

    public void setSharedFlag(boolean sharedFlag) {
        if (this.sharedFlag != sharedFlag) {
            this.sharedFlag = sharedFlag;
            changedSettingsMask |= CHANGED_SHARED;
        }
    }

    public boolean isViewOnly() {
        return viewOnly;
    }

    public void setViewOnly(boolean viewOnly) {
        if (this.viewOnly != viewOnly) {
            this.viewOnly = viewOnly;
            changedSettingsMask |= CHANGED_VIEW_ONLY;
        }
    }

    public int getColorDepth() {
        return colorDepth;
    }

    /**
     * Set depth only in 3, 6, 8, 16, 32. When depth is wrong, it resets to
     * {@link #DEFAULT_COLOR_DEPTH}
     */
    public void setColorDepth(int depth) {
        if (colorDepth != depth) {
            changedSettingsMask |= CHANGED_COLOR_DEPTH | CHANGED_ENCODINGS;
            colorDepth = switch (depth) {
                case COLOR_DEPTH_32 -> COLOR_DEPTH_24;
                case COLOR_DEPTH_24, COLOR_DEPTH_16, COLOR_DEPTH_8, COLOR_DEPTH_6, COLOR_DEPTH_3, COLOR_DEPTH_SERVER_SETTINGS -> depth;
                default -> DEFAULT_COLOR_DEPTH;
            };
        }
    }

    public void fireListeners() {
        if (null == listeners) {
            return;
        }
        final var event = new TS_LibVncDesktopCore_SettingsChangedEvent(new TS_LibVncDesktopRfbProtocol_Settings(this));
        changedSettingsMask = 0;
        listeners.forEach(listener -> {
            listener.settingsChanged(event);
        });
    }

    public static boolean isRfbSettingsChangedFired(TS_LibVncDesktopCore_SettingsChangedEvent event) {
        return event.getSource() instanceof TS_LibVncDesktopRfbProtocol_Settings;
    }

    public void setPreferredEncoding(TS_LibVncDesktopRfbEncoding_Type preferredEncoding) {
        if (this.preferredEncoding != preferredEncoding) {
            this.preferredEncoding = preferredEncoding;
            changedSettingsMask |= CHANGED_ENCODINGS;
        }
    }

    public TS_LibVncDesktopRfbEncoding_Type getPreferredEncoding() {
        return preferredEncoding;
    }

    public void setAllowCopyRect(boolean allowCopyRect) {
        if (this.allowCopyRect != allowCopyRect) {
            this.allowCopyRect = allowCopyRect;
            changedSettingsMask |= CHANGED_ALLOW_COPY_RECT | CHANGED_ENCODINGS;
        }
    }

    public boolean isAllowCopyRect() {
        return allowCopyRect;
    }

    private void setShowRemoteCursor(boolean showRemoteCursor) {
        if (this.showRemoteCursor != showRemoteCursor) {
            this.showRemoteCursor = showRemoteCursor;
            changedSettingsMask |= CHANGED_SHOW_REMOTE_CURSOR | CHANGED_ENCODINGS;
        }
    }

    public boolean isShowRemoteCursor() {
        return showRemoteCursor;
    }

    public void setMouseCursorTrack(TS_LibVncDesktopRfbProtocol_LocalPointer mouseCursorTrack) {
        if (this.mouseCursorTrack != mouseCursorTrack) {
            this.mouseCursorTrack = mouseCursorTrack;
            changedSettingsMask |= CHANGED_MOUSE_CURSOR_TRACK | CHANGED_ENCODINGS;
            setShowRemoteCursor(TS_LibVncDesktopRfbProtocol_LocalPointer.ON == mouseCursorTrack);
        }
    }

    public TS_LibVncDesktopRfbProtocol_LocalPointer getMouseCursorTrack() {
        return mouseCursorTrack;
    }

    public int setCompressionLevel(int compressionLevel) {
        if (compressionLevel >= MIN_COMPRESSION_LEVEL && compressionLevel <= MAX_COMPRESSION_LEVEL
                && this.compressionLevel != compressionLevel) {
            this.compressionLevel = compressionLevel;
            changedSettingsMask |= CHANGED_COMPRESSION_LEVEL | CHANGED_ENCODINGS;
        }
        return this.compressionLevel;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public int setJpegQuality(int jpegQuality) {
        if (jpegQuality >= MIN_JPEG_QUALITY && jpegQuality <= MAX_JPEG_QUALITY
                && this.jpegQuality != jpegQuality) {
            this.jpegQuality = jpegQuality;
            changedSettingsMask |= CHANGED_JPEG_QUALITY | CHANGED_ENCODINGS;
        }
        return this.jpegQuality;
    }

    public int getJpegQuality() {
        return jpegQuality;
    }

    public void setAllowClipboardTransfer(boolean enable) {
        if (this.allowClipboardTransfer != enable) {
            this.allowClipboardTransfer = enable;
            changedSettingsMask |= CHANGED_ALLOW_CLIPBOARD_TRANSFER;
        }
    }

    public boolean isAllowClipboardTransfer() {
        return allowClipboardTransfer;
    }

    public boolean isConvertToAscii() {
        return convertToAscii;
    }

    public void setConvertToAscii(boolean convertToAscii) {
        if (this.convertToAscii != convertToAscii) {
            this.convertToAscii = convertToAscii;
            changedSettingsMask |= CHANGED_CONVERT_TO_ASCII;
        }
    }

    public boolean isChangedEncodings() {
        return (changedSettingsMask & CHANGED_ENCODINGS) == CHANGED_ENCODINGS;
    }

    public boolean isChangedColorDepth() {
        return (changedSettingsMask & CHANGED_COLOR_DEPTH) == CHANGED_COLOR_DEPTH;
    }

    public void setRemoteCharsetName(String remoteCharsetName) {
        this.remoteCharsetName = remoteCharsetName;
    }

    public String getRemoteCharsetName() {
        return remoteCharsetName;
    }

    @Override
    public String toString() {
        return "ProtocolSettings{"
                + "sharedFlag=" + sharedFlag
                + ", viewOnly=" + viewOnly
                + ", preferredEncoding=" + preferredEncoding
                + ", allowCopyRect=" + allowCopyRect
                + ", showRemoteCursor=" + showRemoteCursor
                + ", mouseCursorTrack=" + mouseCursorTrack
                + ", compressionLevel=" + compressionLevel
                + ", jpegQuality=" + jpegQuality
                + ", allowClipboardTransfer=" + allowClipboardTransfer
                + ", convertToAscii=" + convertToAscii
                + ", colorDepth=" + colorDepth
                + '}';
    }

    public TS_LibVncDesktopRfbProtocol_TunnelType getTunnelType() {
        return tunnelType;
    }

    public void setTunnelType(TS_LibVncDesktopRfbProtocol_TunnelType tunnelType) {
        this.tunnelType = tunnelType;
    }
}
