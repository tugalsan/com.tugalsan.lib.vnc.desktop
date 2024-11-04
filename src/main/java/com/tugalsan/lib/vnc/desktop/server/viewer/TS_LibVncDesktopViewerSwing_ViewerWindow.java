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

import com.tugalsan.api.desktop.server.TS_DesktopDialogInfoUtils;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopCore_SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbClient_KeyEventMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Protocol;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Settings;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_TunnelType;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopUtils_Keymap;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_SettingsUi;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfb_IChangeSettingsListener;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfb_IRepaintController;

public class TS_LibVncDesktopViewerSwing_ViewerWindow implements TS_LibVncDesktopRfb_IChangeSettingsListener, TS_LibVncDesktopViewerSwing_MouseEnteredListener {

    public static final int FS_SCROLLING_ACTIVE_BORDER = 20;
    private JToggleButton zoomFitButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomAsIsButton;
    private JScrollPane scroller;
    private JInternalFrame frame;
    private ButtonsBar buttonsBar;
    private final TS_LibVncDesktopViewerSwing_Surface surface;
    private final TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener viewerEventsListener;
    private final String connectionString;
    private final TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter;
    private JLayeredPane lpane;
    private EmptyButtonsBarMouseAdapter buttonsBarMouseAdapter;
    private String remoteDesktopName;
    private final TS_LibVncDesktopRfbProtocol_Settings rfbSettings;
    private final TS_LibVncDesktopViewer_SettingsUi uiSettings;
    private final TS_LibVncDesktopRfbProtocol_Protocol workingProtocol;
    private final JDesktopPane pane;
    private final Window window;

    private boolean isZoomToFitSelected;
    private List<JComponent> kbdButtons;
    private Container container;

    public TS_LibVncDesktopViewerSwing_ViewerWindow(TS_LibVncDesktopRfbProtocol_Protocol workingProtocol, TS_LibVncDesktopRfbProtocol_Settings rfbSettings, TS_LibVncDesktopViewer_SettingsUi uiSettings, TS_LibVncDesktopViewerSwing_Surface surface, TS_LibVncDesktopViewerSwing_ViewerViewerEventsListener viewerEventsListener, String connectionString, TS_LibVncDesktopViewerSwing_ConnectionPresenter presenter, JDesktopPane pane, Window window) {
        this.window = window;
        this.pane = pane;
        this.workingProtocol = workingProtocol;
        this.rfbSettings = rfbSettings;
        this.uiSettings = uiSettings;
        this.surface = surface;
        this.viewerEventsListener = viewerEventsListener;
        this.connectionString = connectionString;
        this.presenter = presenter;
        createContainer(surface);

        if (uiSettings.showControls) {
            createButtonsPanel(workingProtocol, frame);
            registerResizeListener(frame);
            updateZoomButtonsState();
        }
        setSurfaceToHandleKbdFocus();
    }

    private void createContainer(final TS_LibVncDesktopViewerSwing_Surface surface) {
        lpane = new JLayeredPane() {
            @Override
            public Dimension getSize() {
                return surface.getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return surface.getPreferredSize();
            }
        };
        lpane.setPreferredSize(surface.getPreferredSize());
        lpane.add(surface, JLayeredPane.DEFAULT_LAYER, 0);
        scroller = new JScrollPane();
        scroller.getViewport().setBackground(Color.DARK_GRAY);
        scroller.setViewportView(lpane);

        frame = new JInternalFrame();
        pane.add(frame);
        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                super.internalFrameClosing(e); //To change body of generated methods, choose Tools | Templates.
                fireCloseApp();
                pane.remove(frame);
            }
        });
        //frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        //Utils.setApplicationIconsForWindow(frame);
        frame.setLayout(new BorderLayout(0, 0));
        frame.add(scroller, BorderLayout.CENTER);

//			frame.pack();
        lpane.setSize(surface.getPreferredSize());

        internalPack(null);
        container = frame;
        fireContainerCompleted();
        frame.setResizable(true);
    }

    private void fireContainerCompleted() {
        if (viewerEventsListener != null) {
            viewerEventsListener.onViewerComponentContainerBuilt(this);
        }
    }

    public void pack() {
        var oldSize = lpane.getSize();
        lpane.setSize(surface.getPreferredSize());
        if (!isZoomToFitSelected()) {
            internalPack(oldSize);
        }
        if (buttonsBar != null) {
            updateZoomButtonsState();
        }
        updateWindowTitle();
    }

    public boolean isZoomToFitSelected() {
        return isZoomToFitSelected;
    }

    public void setZoomToFitSelected(boolean zoomToFitSelected) {
        isZoomToFitSelected = zoomToFitSelected;
    }

    public void setRemoteDesktopName(String name) {
        remoteDesktopName = name;
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        frame.setTitle(remoteDesktopName + " - " + connectionString + " - [zoom: " + uiSettings.getScalePercentFormatted() + "%]");
    }

    private void internalPack(Dimension outerPanelOldSize) {
        var workareaRectangle = getWorkareaRectangle();
        var isHScrollBar = scroller.getHorizontalScrollBar().isShowing();
        var isVScrollBar = scroller.getVerticalScrollBar().isShowing();

        var isWidthChangeable = true;
        var isHeightChangeable = true;
        if (outerPanelOldSize != null && surface.oldSize != null) {
            isWidthChangeable = (outerPanelOldSize.width == surface.oldSize.width && !isHScrollBar);
            isHeightChangeable = (outerPanelOldSize.height == surface.oldSize.height && !isVScrollBar);
        }
        frame.validate();

        var containerInsets = frame.getInsets();
        var preferredSize = frame.getPreferredSize();
        var preferredRectangle = new Rectangle(frame.getLocation(), preferredSize);

        if (null == outerPanelOldSize && workareaRectangle.contains(preferredRectangle)) {
            frame.pack();
        } else {
            var minDimension = new Dimension(
                    containerInsets.left + containerInsets.right, containerInsets.top + containerInsets.bottom);
            if (buttonsBar != null && buttonsBar.isVisible) {
                minDimension.width += buttonsBar.getWidth();
                minDimension.height += buttonsBar.getHeight();
            }
            var dim = new Dimension(preferredSize);
            var location = frame.getLocation();
            if (!isWidthChangeable) {
                dim.width = frame.getWidth();
            } else {
                if (isVScrollBar) {
                    dim.width += scroller.getVerticalScrollBar().getWidth();
                }
                if (dim.width < minDimension.width) {
                    dim.width = minDimension.width;
                }

                var dx = location.x - workareaRectangle.x;
                if (dx < 0) {
                    dx = 0;
                    location.x = workareaRectangle.x;
                }
                var w = workareaRectangle.width - dx;
                if (w < dim.width) {
                    var dw = dim.width - w;
                    if (dw < dx) {
                        location.x -= dw;
                    } else {
                        dim.width = workareaRectangle.width;
                        location.x = workareaRectangle.x;
                    }
                }
            }
            if (!isHeightChangeable) {
                dim.height = frame.getHeight();
            } else {

                if (isHScrollBar) {
                    dim.height += scroller.getHorizontalScrollBar().getHeight();
                }
                if (dim.height < minDimension.height) {
                    dim.height = minDimension.height;
                }

                var dy = location.y - workareaRectangle.y;
                if (dy < 0) {
                    dy = 0;
                    location.y = workareaRectangle.y;
                }
                var h = workareaRectangle.height - dy;
                if (h < dim.height) {
                    var dh = dim.height - h;
                    if (dh < dy) {
                        location.y -= dh;
                    } else {
                        dim.height = workareaRectangle.height;
                        location.y = workareaRectangle.y;
                    }
                }
            }
            if (!location.equals(frame.getLocation())) {
                frame.setLocation(location);
            }
            frame.setSize(dim);
        }
        scroller.revalidate();
    }

    private Rectangle getWorkareaRectangle() {
        var graphicsConfiguration = frame.getGraphicsConfiguration();
        var screenBounds = graphicsConfiguration.getBounds();
        var screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= screenInsets.left + screenInsets.right;
        screenBounds.height -= screenInsets.top + screenInsets.bottom;
        return screenBounds;
    }

    void addZoomButtons() {
        buttonsBar.createStrut();
        zoomOutButton = buttonsBar.createButton("zoom-out", "Zoom Out", (ActionEvent e) -> {
            zoomFitButton.setSelected(false);
            uiSettings.zoomOut();
        });
        zoomInButton = buttonsBar.createButton("zoom-in", "Zoom In", (ActionEvent e) -> {
            zoomFitButton.setSelected(false);
            uiSettings.zoomIn();
        });
        zoomAsIsButton = buttonsBar.createButton("zoom-100", "Zoom 100%", (ActionEvent e) -> {
            zoomFitButton.setSelected(false);
            uiSettings.zoomAsIs();
        });

        {
            zoomFitButton = buttonsBar.createToggleButton("zoom-fit", "Zoom to Fit Window", (ItemEvent e) -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setZoomToFitSelected(true);
                    zoomToFit();
                    updateZoomButtonsState();
                } else {
                    setZoomToFitSelected(false);
                }
                setSurfaceToHandleKbdFocus();
            });
            zoomFitButton.setEnabled(true);
            zoomFitButton.setSelected(true);
            setZoomToFitSelected(true);
        }
    }

    final protected void setSurfaceToHandleKbdFocus() {
        if (surface != null && !surface.requestFocusInWindow()) {
            surface.requestFocus();
        }
    }

    private void zoomToFit() {
        var scrollerSize = scroller.getSize();
        var scrollerInsets = scroller.getInsets();
        uiSettings.zoomToFit(scrollerSize.width - scrollerInsets.left - scrollerInsets.right,
                scrollerSize.height - scrollerInsets.top - scrollerInsets.bottom,
                workingProtocol.getFbWidth(), workingProtocol.getFbHeight());
    }

    final void registerResizeListener(Container container) {
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ReDrawOnResize();
            }
        });
    }

    public void ReDrawOnResize() {
        if (isZoomToFitSelected()) {
            zoomToFit();
            updateZoomButtonsState();
            updateWindowTitle();
            setSurfaceToHandleKbdFocus();
        }
    }

    final void updateZoomButtonsState() {
        zoomOutButton.setEnabled(uiSettings.getScalePercent() > TS_LibVncDesktopViewer_SettingsUi.MIN_SCALE_PERCENT);
        zoomInButton.setEnabled(uiSettings.getScalePercent() < TS_LibVncDesktopViewer_SettingsUi.MAX_SCALE_PERCENT);
        zoomAsIsButton.setEnabled(uiSettings.getScalePercent() != 100);
    }

    public ButtonsBar createButtonsBar() {
        buttonsBar = new ButtonsBar();
        return buttonsBar;
    }

    public void setButtonsBarVisible(boolean isVisible) {
        setButtonsBarVisible(isVisible, frame);
    }

    private void setButtonsBarVisible(boolean isVisible, Container container) {
        buttonsBar.setVisible(isVisible);
        if (isVisible) {
            buttonsBar.borderOff();
            container.add(buttonsBar.bar, BorderLayout.NORTH);
            container.validate();
        } else {
            container.remove(buttonsBar.bar);
            buttonsBar.borderOn();
        }
    }

    public void setButtonsBarVisibleFS(boolean isVisible) {
        if (isVisible) {
            if (!buttonsBar.isVisible) {
                lpane.add(buttonsBar.bar, JLayeredPane.POPUP_LAYER, 0);
                var bbWidth = buttonsBar.bar.getPreferredSize().width;
                buttonsBar.bar.setBounds(
                        scroller.getViewport().getViewPosition().x + (scroller.getWidth() - bbWidth) / 2, 0,
                        bbWidth, buttonsBar.bar.getPreferredSize().height);

                // prevent mouse events to through down to Surface
                if (null == buttonsBarMouseAdapter) {
                    buttonsBarMouseAdapter = new EmptyButtonsBarMouseAdapter();
                }
                buttonsBar.bar.addMouseListener(buttonsBarMouseAdapter);
            }
        } else {
            buttonsBar.bar.removeMouseListener(buttonsBarMouseAdapter);
            lpane.remove(buttonsBar.bar);
            lpane.repaint(buttonsBar.bar.getBounds());
        }
        buttonsBar.setVisible(isVisible);
        lpane.repaint();
        lpane.validate();
        buttonsBar.bar.validate();
    }

    public TS_LibVncDesktopRfb_IRepaintController getRepaintController() {
        return surface;
    }

    void close() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    @Override
    public void mouseEnteredEvent(MouseEvent mouseEvent) {
        setSurfaceToHandleKbdFocus();
    }

    public void addMouseListener(MouseListener mouseListener) {
        surface.addMouseListener(mouseListener);
    }

    public JInternalFrame getFrame() {
        return frame;
    }

    public void setVisible() {
        container.setVisible(true);
    }

    public void validate() {
        container.validate();
    }

    public static class ButtonsBar {

        private static final Insets BUTTONS_MARGIN = new Insets(2, 2, 2, 2);
        private JPanel bar;
        private boolean isVisible;
        private final ArrayList<Component> noFullScreenGroup = new ArrayList();

        public ButtonsBar() {
            bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        }

        public JButton createButton(String iconId, String tooltipText, ActionListener actionListener) {
            var button = new JButton(iconId);
            button.setToolTipText(tooltipText);
            button.setMargin(BUTTONS_MARGIN);
            bar.add(button);
            button.addActionListener(actionListener);
            return button;
        }

        public Component createStrut() {
            return bar.add(Box.createHorizontalStrut(10));
        }

        public JToggleButton createToggleButton(String iconId, String tooltipText, ItemListener itemListener) {
            var button = new JToggleButton(iconId);
            button.setToolTipText(tooltipText);
            button.setMargin(BUTTONS_MARGIN);
            bar.add(button);
            button.addItemListener(itemListener);
            return button;
        }

        public void setVisible(boolean isVisible) {
            this.isVisible = isVisible;
            if (isVisible) {
                bar.revalidate();
            }
        }

        public int getWidth() {
            return bar.getMinimumSize().width;
        }

        public int getHeight() {
            return bar.getMinimumSize().height;
        }

        public void borderOn() {
            bar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        public void borderOff() {
            bar.setBorder(BorderFactory.createEmptyBorder());
        }

        public void addToNoFullScreenGroup(Component component) {
            noFullScreenGroup.add(component);
        }

        public void setNoFullScreenGroupVisible(boolean isVisible) {
            noFullScreenGroup.forEach(c -> {
                c.setVisible(isVisible);
            });
        }
    }

    private static class EmptyButtonsBarMouseAdapter extends MouseAdapter {
        // empty
    }

    final protected void createButtonsPanel(final TS_LibVncDesktopRfbProtocol_Protocol protocol, Container container) {
       var bb = createButtonsBar();

        bb.addToNoFullScreenGroup(
                bb.createButton("options", "Set Options", (ActionEvent e) -> {
                    showOptionsDialog();
                    setSurfaceToHandleKbdFocus();
                }));

        bb.addToNoFullScreenGroup(
                bb.createButton("info", "Show connection info", (ActionEvent e) -> {
                    showConnectionInfoMessage();
                    setSurfaceToHandleKbdFocus();
                }));

        bb.addToNoFullScreenGroup(
                bb.createStrut());

        bb.createButton("refresh", "Refresh screen", (ActionEvent e) -> {
            protocol.sendRefreshMessage();
            setSurfaceToHandleKbdFocus();
        });

        addZoomButtons();

        kbdButtons = new LinkedList();

        bb.createStrut();

        var ctrlAltDelButton = bb.createButton("ctrl-alt-del", "Send 'Ctrl-Alt-Del'", (ActionEvent e) -> {
            sendCtrlAltDel(protocol);
            setSurfaceToHandleKbdFocus();
        });
        kbdButtons.add(ctrlAltDelButton);

        var winButton = bb.createButton("win", "Send 'Win' key as 'Ctrl-Esc'", (ActionEvent e) -> {
            sendWinKey(protocol);
            setSurfaceToHandleKbdFocus();
        });
        kbdButtons.add(winButton);

        var ctrlButton = bb.createToggleButton("ctrl", "Ctrl Lock", (ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_CTRL_LEFT, true));
            } else {
                protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_CTRL_LEFT, false));
            }
            setSurfaceToHandleKbdFocus();
        });
        kbdButtons.add(ctrlButton);

        var altButton = bb.createToggleButton("alt", "Alt Lock", (ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_ALT_LEFT, true));
            } else {
                protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_ALT_LEFT, false));
            }
            setSurfaceToHandleKbdFocus();
        });
        kbdButtons.add(altButton);

        var modifierButtonListener = new TS_LibVncDesktopViewerSwing_ModifierButtonEventListener();
        modifierButtonListener.addButton(KeyEvent.VK_CONTROL, ctrlButton);
        modifierButtonListener.addButton(KeyEvent.VK_ALT, altButton);
        surface.addModifierListener(modifierButtonListener);

//		JButton fileTransferButton = new JButton(SwingUtils.getButtonIcon("file-transfer"));
//		fileTransferButton.setMargin(buttonsMargin);
//		buttonBar.add(fileTransferButton);
//        buttonsBar.createStrut();
//
        var viewOnlyButton = bb.createToggleButton("viewonly", "View Only", (ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                rfbSettings.setViewOnly(true);
                rfbSettings.fireListeners();
            } else {
                rfbSettings.setViewOnly(false);
                rfbSettings.fireListeners();
            }
            setSurfaceToHandleKbdFocus();
        });
        viewOnlyButton.setSelected(rfbSettings.isViewOnly());
        rfbSettings.addListener((TS_LibVncDesktopCore_SettingsChangedEvent event) -> {
            if (TS_LibVncDesktopRfbProtocol_Settings.isRfbSettingsChangedFired(event)) {
                var settings = (TS_LibVncDesktopRfbProtocol_Settings) event.getSource();
                viewOnlyButton.setSelected(settings.isViewOnly());
            }
        });
        kbdButtons.add(viewOnlyButton);
        bb.createStrut();

        bb.createButton("close", "Close", (ActionEvent e) -> {
            close();
            presenter.setNeedReconnection(false);
            presenter.cancelConnection();
            fireCloseApp();
        }).setAlignmentX(JComponent.RIGHT_ALIGNMENT);

        setButtonsBarVisible(true, container);
    }

    private void fireCloseApp() {
        if (viewerEventsListener != null) {
            viewerEventsListener.onViewerComponentClosing();
        }
    }

    private void sendCtrlAltDel(TS_LibVncDesktopRfbProtocol_Protocol protocol) {
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_CTRL_LEFT, true));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_ALT_LEFT, true));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_DELETE, true));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_DELETE, false));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_ALT_LEFT, false));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_CTRL_LEFT, false));
    }

    private void sendWinKey(TS_LibVncDesktopRfbProtocol_Protocol protocol) {
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_CTRL_LEFT, true));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_ESCAPE, true));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_ESCAPE, false));
        protocol.sendMessage(new TS_LibVncDesktopRfbClient_KeyEventMessage(TS_LibVncDesktopUtils_Keymap.K_CTRL_LEFT, false));
    }

    @Override
    public void settingsChanged(TS_LibVncDesktopCore_SettingsChangedEvent e) {
        if (TS_LibVncDesktopRfbProtocol_Settings.isRfbSettingsChangedFired(e)) {
            var settings = (TS_LibVncDesktopRfbProtocol_Settings) e.getSource();
            setEnabledKbdButtons(!settings.isViewOnly());
        }
    }

    private void setEnabledKbdButtons(boolean enabled) {
        if (kbdButtons != null) {
            kbdButtons.forEach(b -> {
                b.setEnabled(enabled);
            });
        }
    }

    private void showOptionsDialog() {
        var optionsDialog = new TS_LibVncDesktopViewerSwing_GuiOptionsDialog(window);
        optionsDialog.initControlsFromSettings(rfbSettings, uiSettings, false);
        optionsDialog.setVisible(true);
    }

    private void showConnectionInfoMessage() {
        var message = new StringBuilder();
        message.append("Connected to: ").append(remoteDesktopName).append("\n");
        message.append("Host: ").append(connectionString).append("\n\n");

        message.append("Desktop geometry: ")
                .append(String.valueOf(surface.getWidth()))
                .append(" \u00D7 ") // multiplication sign
                .append(String.valueOf(surface.getHeight())).append("\n");
        message.append("Color format: ")
                .append(String.valueOf(Math.round(Math.pow(2, workingProtocol.getPixelFormat().depth))))
                .append(" colors (")
                .append(String.valueOf(workingProtocol.getPixelFormat().depth))
                .append(" bits)\n");
        message.append("Current protocol version: ")
                .append(workingProtocol.getProtocolVersion());
        if (workingProtocol.isTight()) {
            message.append(" tight");
            if (workingProtocol.getTunnelType() != null && workingProtocol.getTunnelType() != TS_LibVncDesktopRfbProtocol_TunnelType.NOTUNNEL) {
                message.append(" using ").append(workingProtocol.getTunnelType().hrName).append(" tunneling");
            }
        }
        message.append("\n");
        TS_DesktopDialogInfoUtils.show("VNC Connection Dialog", message.toString());
    }
}
