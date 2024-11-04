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

import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopCore_SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopDrawing_Renderer;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_PixelFormat;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Protocol;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Settings;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopTransport_Transport;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_SettingsUi;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfb_IChangeSettingsListener;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfb_IRepaintController;

@SuppressWarnings("serial")
public class TS_LibVncDesktopViewerSwing_Surface extends JPanel implements TS_LibVncDesktopRfb_IRepaintController, TS_LibVncDesktopRfb_IChangeSettingsListener {

    private int width;
    private int height;
    private TS_LibVncDesktopViewerSwing_SoftCursorImpl cursor;
    private volatile TS_LibVncDesktopViewerSwing_RendererImpl renderer;
    private TS_LibVncDesktopViewerSwing_MouseEventListener mouseEventListener;
    private TS_LibVncDesktopViewerSwing_KeyEventListener keyEventListener;
    private boolean showCursor;
    private TS_LibVncDesktopViewerSwing_ModifierButtonEventListener modifierButtonListener;
    private boolean isUserInputEnabled = false;
    private final TS_LibVncDesktopRfbProtocol_Protocol protocol;
    private TS_LibVncDesktopViewerSwing_ViewerWindow viewerWindow;
    private double scaleFactor;
    public Dimension oldSize;

    @Override
    public boolean isDoubleBuffered() {
        // TODO returning false in some reason may speed ups drawing, but may
        // not. Needed in challenging.
        return false;
    }

    public TS_LibVncDesktopViewerSwing_Surface(TS_LibVncDesktopRfbProtocol_Protocol protocol, double scaleFactor) {
        this.protocol = protocol;
        this.scaleFactor = scaleFactor;
        init(protocol.getFbWidth(), protocol.getFbHeight());
        oldSize = getPreferredSize();

        if (!protocol.getSettings().isViewOnly()) {
            setUserInputEnabled(true, protocol.getSettings().isConvertToAscii());
        }
        showCursor = protocol.getSettings().isShowRemoteCursor();
    }

    // TODO Extract abstract/interface ViewerWindow from SwingViewerWindow
    public void setViewerWindow(TS_LibVncDesktopViewerSwing_ViewerWindow viewerWindow) {
        this.viewerWindow = viewerWindow;
    }

    private void setUserInputEnabled(boolean enable, boolean convertToAscii) {
        if (enable == isUserInputEnabled) {
            return;
        }
        isUserInputEnabled = enable;
        if (enable) {
            if (null == mouseEventListener) {
                mouseEventListener = new TS_LibVncDesktopViewerSwing_MouseEventListener(this, protocol, scaleFactor);
            }
            addMouseListener(mouseEventListener);
            addMouseMotionListener(mouseEventListener);
            addMouseWheelListener(mouseEventListener);

            setFocusTraversalKeysEnabled(false);
            if (null == keyEventListener) {
                keyEventListener = new TS_LibVncDesktopViewerSwing_KeyEventListener(protocol);
                if (modifierButtonListener != null) {
                    keyEventListener.addModifierListener(modifierButtonListener);
                }
            }
            keyEventListener.setConvertToAscii(convertToAscii);
            addKeyListener(keyEventListener);
            enableInputMethods(false);
        } else {
            removeMouseListener(mouseEventListener);
            removeMouseMotionListener(mouseEventListener);
            removeMouseWheelListener(mouseEventListener);
            removeKeyListener(keyEventListener);
        }
    }

    @Override
    public TS_LibVncDesktopDrawing_Renderer createRenderer(TS_LibVncDesktopTransport_Transport transport, int width, int height, TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
        renderer = new TS_LibVncDesktopViewerSwing_RendererImpl(transport, width, height, pixelFormat);
        cursor = renderer.getCursor();
        if (SwingUtilities.isEventDispatchThread()) {
            init(renderer.getWidth(), renderer.getHeight());
            updateFrameSize();
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    init(renderer.getWidth(), renderer.getHeight());
                    updateFrameSize();
                });
            } catch (InterruptedException e) {
                Logger.getLogger(getClass().getName()).severe("Interrupted: %s".formatted(e.getMessage()));
                protocol.cleanUpSession("Interrupted: " + e.getMessage());
            } catch (InvocationTargetException e) {
                Logger.getLogger(getClass().getName()).severe("Fatal error: %s".formatted(e.getCause().getMessage()));
                protocol.cleanUpSession("Fatal error: " + e.getCause().getMessage());
            }
        }
        return renderer;
    }

    private void init(int width, int height) {
        this.width = width;
        this.height = height;
        setSize(getPreferredSize());
    }

    public void updateFrameSize() {
        setSize(getPreferredSize());
        viewerWindow.pack();
        requestFocus();
    }

    @Override
    public void paintComponent(Graphics g) { // EDT
        if (null == renderer) {
            return;
        }
        if (scaleFactor != 1.0) {
            ((Graphics2D) g).scale(scaleFactor, scaleFactor);
        }
        var appleContentScaleFactor = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.contentScaleFactor");
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
                (appleContentScaleFactor != null && (Integer) appleContentScaleFactor != 1)
                        ? RenderingHints.VALUE_RENDER_SPEED
                        : // speed for Apple Retina display
                        RenderingHints.VALUE_RENDER_QUALITY); // quality for others
        renderer.paintImageOn(g); // internally locked with renderer.lock
        if (showCursor) {
            renderer.paintCursorOn(g, scaleFactor != 1);// internally locked with cursor.lock
        }
    }

    @Override
    final public Dimension getPreferredSize() {
        return new Dimension((int) (this.width * scaleFactor), (int) (this.height * scaleFactor));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Saves protocol and simply invokes native JPanel repaint method which
     * asyncroniously register repaint request using invokeLater to repaint be
     * runned in Swing event dispatcher thread. So may be called from other
     * threads.
     */
    @Override
    public void repaintBitmap(TS_LibVncDesktopRfbEncoding_FramebufferUpdateRectangle rect) {
        repaintBitmap(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public void repaintBitmap(int x, int y, int width, int height) {
        repaint((int) (x * scaleFactor), (int) (y * scaleFactor),
                (int) Math.ceil(width * scaleFactor), (int) Math.ceil(height * scaleFactor));
    }

    @Override
    public void repaintCursor() {
        synchronized (cursor.getLock()) {
            repaint((int) (cursor.oldRX * scaleFactor), (int) (cursor.oldRY * scaleFactor),
                    (int) Math.ceil(cursor.oldWidth * scaleFactor) + 1, (int) Math.ceil(cursor.oldHeight * scaleFactor) + 1);
            repaint((int) (cursor.rX * scaleFactor), (int) (cursor.rY * scaleFactor),
                    (int) Math.ceil(cursor.width * scaleFactor) + 1, (int) Math.ceil(cursor.height * scaleFactor) + 1);
        }
    }

    @Override
    public void updateCursorPosition(short x, short y) {
        synchronized (cursor.getLock()) {
            cursor.updatePosition(x, y);
            repaintCursor();
        }
    }

    private void showCursor(boolean show) {
        synchronized (cursor.getLock()) {
            showCursor = show;
        }
    }

    public void addModifierListener(TS_LibVncDesktopViewerSwing_ModifierButtonEventListener modifierButtonListener) {
        this.modifierButtonListener = modifierButtonListener;
        if (keyEventListener != null) {
            keyEventListener.addModifierListener(modifierButtonListener);
        }
    }

    @Override
    public void settingsChanged(TS_LibVncDesktopCore_SettingsChangedEvent e) {
        if (TS_LibVncDesktopRfbProtocol_Settings.isRfbSettingsChangedFired(e)) {
            var settings = (TS_LibVncDesktopRfbProtocol_Settings) e.getSource();
            setUserInputEnabled(!settings.isViewOnly(), settings.isConvertToAscii());
            showCursor(settings.isShowRemoteCursor());
        } else if (TS_LibVncDesktopViewer_SettingsUi.isUiSettingsChangedFired(e)) {
            var uiSettings = (TS_LibVncDesktopViewer_SettingsUi) e.getSource();
            oldSize = getPreferredSize();
            scaleFactor = uiSettings.getScaleFactor();
        }
        mouseEventListener.setScaleFactor(scaleFactor);
        updateFrameSize();
    }

    @Override
    public void setPixelFormat(TS_LibVncDesktopRfbEncoding_PixelFormat pixelFormat) {
        if (renderer != null) {
            renderer.initColorDecoder(pixelFormat);
        }
    }
}
