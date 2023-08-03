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
package com.tugalsan.lib.vnc.desktop.server.viewer.swing;

import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.lib.vnc.desktop.server.core.SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.rfb.ClipboardController;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.ClientCutTextMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.Protocol;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.ProtocolSettings;
import com.tugalsan.lib.vnc.desktop.server.utils.Strings;
import com.tugalsan.api.thread.server.async.TS_ThreadAsync;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.Charset;

public class ClipboardControllerImpl implements ClipboardController, Runnable {

    private static final String STANDARD_CHARSET = "ISO-8859-1"; // aka Latin-1
    private static final long CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS = 1000L;
    private Clipboard clipboard;
    private String clipboardText = null;
    private volatile boolean isRunning;
    private boolean isEnabled;
    private final Protocol protocol;
    private final TS_ThreadSyncTrigger killTrigger;
    private Charset charset;

    public ClipboardControllerImpl(TS_ThreadSyncTrigger killTrigger, Protocol protocol, String charsetName) {
        this.killTrigger = killTrigger;
        this.protocol = protocol;
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        updateSavedClipboardContent(); // prevent onstart clipboard content sending

        if (Strings.isTrimmedEmpty(charsetName)) {
            charset = Charset.defaultCharset();
        } else if (TGS_CharSetCast.equalsLocaleIgnoreCase("standard", charsetName)) {
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
            StringSelection stringSelection = new StringSelection(new String(bytes, charset));
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
            } catch (UnsupportedFlavorException e) {
                // ignore
            } catch (IOException e) {
                // ignore
            }
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
        String old = clipboardText;
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
            TS_ThreadAsync.now(killTrigger, kt -> run());
        }
        isEnabled = enable;
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning && killTrigger.hasNotTriggered()) {
            String clipboardText = getRenewedClipboardText();
            if (clipboardText != null) {
                protocol.sendMessage(new ClientCutTextMessage(clipboardText, charset));
            }
            try {
                Thread.sleep(CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS);
            } catch (InterruptedException ignore) {
            }
        }
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        ProtocolSettings settings = (ProtocolSettings) e.getSource();
        setEnabled(settings.isAllowClipboardTransfer());
    }

}
