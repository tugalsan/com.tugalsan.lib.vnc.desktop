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

import module com.tugalsan.api.thread;
import module com.tugalsan.api.charset;
import module com.tugalsan.api.log;
import module java.desktop;
import com.tugalsan.lib.vnc.desktop.server.base.*;
import com.tugalsan.lib.vnc.desktop.server.rfb.*;
import java.io.*;
import java.nio.charset.*;

public class TS_LibVncDesktopViewerSwing_ClipboardControllerImpl implements TS_LibVncDesktopRfb_ClipboardController, Runnable {

    final static private TS_Log d = TS_Log.of(TS_LibVncDesktopViewerSwing_ClipboardControllerImpl.class);

    private static final String STANDARD_CHARSET = "ISO-8859-1"; // aka Latin-1
    private static final long CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS = 1000L;
    private final Clipboard clipboard;
    private String clipboardText = null;
    private volatile boolean isRunning;
    private boolean isEnabled;
    private final TS_LibVncDesktopRfbProtocol_Protocol protocol;
    private final TS_ThreadSyncTrigger killTrigger;
    private Charset charset;

    public TS_LibVncDesktopViewerSwing_ClipboardControllerImpl(TS_ThreadSyncTrigger killTrigger, TS_LibVncDesktopRfbProtocol_Protocol protocol, String charsetName) {
        this.killTrigger = killTrigger;
        this.protocol = protocol;
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        updateSavedClipboardContent(); // prevent onstart clipboard content sending

        if (TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(charsetName)) {
            charset = Charset.defaultCharset();
        } else if (TGS_CharSetCast.current().equalsIgnoreCase("standard", charsetName)) {
            charset = Charset.forName(STANDARD_CHARSET);
        } else {
            charset = Charset.isSupported(charsetName) ? Charset.forName(charsetName) : Charset.defaultCharset();
        }
        // not supported UTF-charsets as they are multibytes.
        // add others multibytes charsets on need
        if (charset.name().startsWith("UTF")) {
            charset = Charset.forName(STANDARD_CHARSET);
        }
    }

    @Override
    public void updateSystemClipboard(byte[] bytes) {
        if (clipboard != null) {
            var stringSelection = new StringSelection(new String(bytes, charset));
            if (isEnabled) {
                clipboard.setContents(stringSelection, null);
            }
        }
    }

    /**
     * Callback for clipboard changes listeners Retrieves text content from
     * system clipboard which then available through getClipboardText().
     */
    private void updateSavedClipboardContent() {
        if (clipboard != null && clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                // ignore
            }
            // ignore

        } else {
            clipboardText = null;
        }
    }

    @Override
    public String getClipboardText() {
        return clipboardText;
    }

    /**
     * Get text clipboard contents when needed send to remote, or null vise
     * versa
     *
     * @return clipboard string contents if it is changed from last method call
     * or null when clipboard contains non text object or clipboard contents
     * didn't changed
     */
    @Override
    public String getRenewedClipboardText() {
        var old = clipboardText;
        updateSavedClipboardContent();
        if (clipboardText != null && !clipboardText.equals(old)) {
            return clipboardText;
        }
        return null;
    }

    @Override
    public void setEnabled(boolean enable) {
        if (!enable) {
            isRunning = false;
        }
        if (enable && !isEnabled) {
            TS_ThreadAsyncRun.now(killTrigger.newChild(d.className()), kt -> run());
        }
        isEnabled = enable;
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning && killTrigger.hasNotTriggered()) {
            var clipboardText = getRenewedClipboardText();
            if (clipboardText != null) {
                protocol.sendMessage(new TS_LibVncDesktopRfbClient_CutTextMessage(clipboardText, charset));
            }
            try {
                Thread.sleep(CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS);
            } catch (InterruptedException ignore) {
            }
        }
    }

    @Override
    public void settingsChanged(TS_LibVncDesktopCore_SettingsChangedEvent e) {
        var settings = (TS_LibVncDesktopRfbProtocol_Settings) e.getSource();
        setEnabled(settings.isAllowClipboardTransfer());
    }

}
