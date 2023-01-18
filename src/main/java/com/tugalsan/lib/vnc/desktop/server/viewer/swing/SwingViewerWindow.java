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

import com.tugalsan.api.desktop.server.TS_DesktopDialogInfoUtils;
import com.tugalsan.lib.vnc.desktop.server.core.SettingsChangedEvent;
import com.tugalsan.lib.vnc.desktop.server.rfb.IChangeSettingsListener;
import com.tugalsan.lib.vnc.desktop.server.rfb.IRepaintController;
import com.tugalsan.lib.vnc.desktop.server.rfb.client.KeyEventMessage;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.Protocol;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.ProtocolSettings;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.tunnel.TunnelType;
import com.tugalsan.lib.vnc.desktop.server.utils.Keymap;
import com.tugalsan.lib.vnc.desktop.server.viewer.settings.UiSettings;
import com.tugalsan.lib.vnc.desktop.server.viewer.swing.gui.OptionsDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class SwingViewerWindow implements IChangeSettingsListener, MouseEnteredListener {

    public static final int FS_SCROLLING_ACTIVE_BORDER = 20;
    private JToggleButton zoomFitButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomAsIsButton;
    private JScrollPane scroller;
    private JInternalFrame frame;
    private ButtonsBar buttonsBar;
    private Surface surface;
    private ViewerEventsListener viewerEventsListener;
    private String connectionString;
    private ConnectionPresenter presenter;
    private JLayeredPane lpane;
    private EmptyButtonsBarMouseAdapter buttonsBarMouseAdapter;
    private String remoteDesktopName;
    private ProtocolSettings rfbSettings;
    private UiSettings uiSettings;
    private Protocol workingProtocol;
    private JDesktopPane pane;
    private Window window;

    private boolean isZoomToFitSelected;
    private List<JComponent> kbdButtons;
    private Container container;
    private static Logger logger = Logger.getLogger(SwingViewerWindow.class.getName());

    public SwingViewerWindow(Protocol workingProtocol, ProtocolSettings rfbSettings, UiSettings uiSettings, Surface surface, ViewerEventsListener viewerEventsListener, String connectionString, ConnectionPresenter presenter, JDesktopPane pane, Window window) {
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

    private void createContainer(final Surface surface) {
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
        final Dimension oldSize = lpane.getSize();
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
        final Rectangle workareaRectangle = getWorkareaRectangle();
        final boolean isHScrollBar = scroller.getHorizontalScrollBar().isShowing();
        final boolean isVScrollBar = scroller.getVerticalScrollBar().isShowing();

        boolean isWidthChangeable = true;
        boolean isHeightChangeable = true;
        if (outerPanelOldSize != null && surface.oldSize != null) {
            isWidthChangeable = (outerPanelOldSize.width == surface.oldSize.width && !isHScrollBar);
            isHeightChangeable = (outerPanelOldSize.height == surface.oldSize.height && !isVScrollBar);
        }
        frame.validate();

        final Insets containerInsets = frame.getInsets();
        Dimension preferredSize = frame.getPreferredSize();
        Rectangle preferredRectangle = new Rectangle(frame.getLocation(), preferredSize);

        if (null == outerPanelOldSize && workareaRectangle.contains(preferredRectangle)) {
            frame.pack();
        } else {
            Dimension minDimension = new Dimension(
                    containerInsets.left + containerInsets.right, containerInsets.top + containerInsets.bottom);
            if (buttonsBar != null && buttonsBar.isVisible) {
                minDimension.width += buttonsBar.getWidth();
                minDimension.height += buttonsBar.getHeight();
            }
            Dimension dim = new Dimension(preferredSize);
            Point location = frame.getLocation();
            if (!isWidthChangeable) {
                dim.width = frame.getWidth();
            } else {
                if (isVScrollBar) {
                    dim.width += scroller.getVerticalScrollBar().getWidth();
                }
                if (dim.width < minDimension.width) {
                    dim.width = minDimension.width;
                }

                int dx = location.x - workareaRectangle.x;
                if (dx < 0) {
                    dx = 0;
                    location.x = workareaRectangle.x;
                }
                int w = workareaRectangle.width - dx;
                if (w < dim.width) {
                    int dw = dim.width - w;
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

                int dy = location.y - workareaRectangle.y;
                if (dy < 0) {
                    dy = 0;
                    location.y = workareaRectangle.y;
                }
                int h = workareaRectangle.height - dy;
                if (h < dim.height) {
                    int dh = dim.height - h;
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
        final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
        final Rectangle screenBounds = graphicsConfiguration.getBounds();
        final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= screenInsets.left + screenInsets.right;
        screenBounds.height -= screenInsets.top + screenInsets.bottom;
        return screenBounds;
    }

    void addZoomButtons() {
        buttonsBar.createStrut();
        zoomOutButton = buttonsBar.createButton("zoom-out", "Zoom Out", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomFitButton.setSelected(false);
                uiSettings.zoomOut();
            }
        });
        zoomInButton = buttonsBar.createButton("zoom-in", "Zoom In", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomFitButton.setSelected(false);
                uiSettings.zoomIn();
            }
        });
        zoomAsIsButton = buttonsBar.createButton("zoom-100", "Zoom 100%", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomFitButton.setSelected(false);
                uiSettings.zoomAsIs();
            }
        });

        {
            zoomFitButton = buttonsBar.createToggleButton("zoom-fit", "Zoom to Fit Window",
                    new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        setZoomToFitSelected(true);
                        zoomToFit();
                        updateZoomButtonsState();
                    } else {
                        setZoomToFitSelected(false);
                    }
                    setSurfaceToHandleKbdFocus();
                }
            });
            zoomFitButton.setEnabled(true);
            zoomFitButton.setSelected(true);
            setZoomToFitSelected(true);
        }
    }

    protected void setSurfaceToHandleKbdFocus() {
        if (surface != null && !surface.requestFocusInWindow()) {
            surface.requestFocus();
        }
    }

    private void zoomToFit() {
        Dimension scrollerSize = scroller.getSize();
        Insets scrollerInsets = scroller.getInsets();
        uiSettings.zoomToFit(scrollerSize.width - scrollerInsets.left - scrollerInsets.right,
                scrollerSize.height - scrollerInsets.top - scrollerInsets.bottom,
                workingProtocol.getFbWidth(), workingProtocol.getFbHeight());
    }

    void registerResizeListener(Container container) {
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

    void updateZoomButtonsState() {
        zoomOutButton.setEnabled(uiSettings.getScalePercent() > UiSettings.MIN_SCALE_PERCENT);
        zoomInButton.setEnabled(uiSettings.getScalePercent() < UiSettings.MAX_SCALE_PERCENT);
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
                final int bbWidth = buttonsBar.bar.getPreferredSize().width;
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

    public IRepaintController getRepaintController() {
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
        private ArrayList<Component> noFullScreenGroup = new ArrayList<Component>();
        
        public ButtonsBar() {
            bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        }

        public JButton createButton(String iconId, String tooltipText, ActionListener actionListener) {
            JButton button = new JButton(iconId);
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
            JToggleButton button = new JToggleButton(iconId);
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
            for (Component c : noFullScreenGroup) {
                c.setVisible(isVisible);
            }
        }
    }

    private static class EmptyButtonsBarMouseAdapter extends MouseAdapter {
        // empty
    }

    protected void createButtonsPanel(final Protocol protocol, Container container) {
        final SwingViewerWindow.ButtonsBar buttonsBar = createButtonsBar();

        buttonsBar.addToNoFullScreenGroup(
                buttonsBar.createButton("options", "Set Options", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showOptionsDialog();
                        setSurfaceToHandleKbdFocus();
                    }
                }));

        buttonsBar.addToNoFullScreenGroup(
                buttonsBar.createButton("info", "Show connection info", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showConnectionInfoMessage();
                        setSurfaceToHandleKbdFocus();
                    }
                }));

        buttonsBar.addToNoFullScreenGroup(
                buttonsBar.createStrut());

        buttonsBar.createButton("refresh", "Refresh screen", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                protocol.sendRefreshMessage();
                setSurfaceToHandleKbdFocus();
            }
        });

        addZoomButtons();

        kbdButtons = new LinkedList<JComponent>();

        buttonsBar.createStrut();

        JButton ctrlAltDelButton = buttonsBar.createButton("ctrl-alt-del", "Send 'Ctrl-Alt-Del'", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCtrlAltDel(protocol);
                setSurfaceToHandleKbdFocus();
            }
        });
        kbdButtons.add(ctrlAltDelButton);

        JButton winButton = buttonsBar.createButton("win", "Send 'Win' key as 'Ctrl-Esc'", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendWinKey(protocol);
                setSurfaceToHandleKbdFocus();
            }
        });
        kbdButtons.add(winButton);

        JToggleButton ctrlButton = buttonsBar.createToggleButton("ctrl", "Ctrl Lock",
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
                } else {
                    protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
                }
                setSurfaceToHandleKbdFocus();
            }
        });
        kbdButtons.add(ctrlButton);

        JToggleButton altButton = buttonsBar.createToggleButton("alt", "Alt Lock",
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    protocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, true));
                } else {
                    protocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, false));
                }
                setSurfaceToHandleKbdFocus();
            }
        });
        kbdButtons.add(altButton);

        ModifierButtonEventListener modifierButtonListener = new ModifierButtonEventListener();
        modifierButtonListener.addButton(KeyEvent.VK_CONTROL, ctrlButton);
        modifierButtonListener.addButton(KeyEvent.VK_ALT, altButton);
        surface.addModifierListener(modifierButtonListener);

//		JButton fileTransferButton = new JButton(SwingUtils.getButtonIcon("file-transfer"));
//		fileTransferButton.setMargin(buttonsMargin);
//		buttonBar.add(fileTransferButton);
//        buttonsBar.createStrut();
//
        final JToggleButton viewOnlyButton = buttonsBar.createToggleButton("viewonly", "View Only", (ItemEvent e) -> {
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
        rfbSettings.addListener((SettingsChangedEvent event) -> {
            if (ProtocolSettings.isRfbSettingsChangedFired(event)) {
                ProtocolSettings settings = (ProtocolSettings) event.getSource();
                viewOnlyButton.setSelected(settings.isViewOnly());
            }
        });
        kbdButtons.add(viewOnlyButton);
        buttonsBar.createStrut();

        buttonsBar.createButton("close", "Close", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
                presenter.setNeedReconnection(false);
                presenter.cancelConnection();
                fireCloseApp();
            }
        }).setAlignmentX(JComponent.RIGHT_ALIGNMENT);

        setButtonsBarVisible(true, container);
    }

    private void fireCloseApp() {
        if (viewerEventsListener != null) {
            viewerEventsListener.onViewerComponentClosing();
        }
    }

    private void sendCtrlAltDel(Protocol protocol) {
        protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, true));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_DELETE, true));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_DELETE, false));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, false));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
    }

    private void sendWinKey(Protocol protocol) {
        protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_ESCAPE, true));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_ESCAPE, false));
        protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        if (ProtocolSettings.isRfbSettingsChangedFired(e)) {
            ProtocolSettings settings = (ProtocolSettings) e.getSource();
            setEnabledKbdButtons(!settings.isViewOnly());
        }
    }

    private void setEnabledKbdButtons(boolean enabled) {
        if (kbdButtons != null) {
            for (JComponent b : kbdButtons) {
                b.setEnabled(enabled);
            }
        }
    }

    private void showOptionsDialog() {
        OptionsDialog optionsDialog = new OptionsDialog(window);
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
            if (workingProtocol.getTunnelType() != null && workingProtocol.getTunnelType() != TunnelType.NOTUNNEL) {
                message.append(" using ").append(workingProtocol.getTunnelType().hrName).append(" tunneling");
            }
        }
        message.append("\n");
        TS_DesktopDialogInfoUtils.show("VNC Connection Dialog", message.toString());
    }
}
