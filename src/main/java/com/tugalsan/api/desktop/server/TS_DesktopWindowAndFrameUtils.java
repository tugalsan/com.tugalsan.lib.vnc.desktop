package com.tugalsan.api.desktop.server;

import com.tugalsan.api.shape.client.TGS_ShapeRectangle;
import java.awt.*;
import javax.swing.*;

public class TS_DesktopWindowAndFrameUtils {

    public static void decorate(Window dialog) {
        dialog.setAlwaysOnTop(true);
        dialog.pack();
        if (dialog instanceof JDialog dia) {
            dia.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        }
        dialog.toFront();
    }

    public static void center(Window window) {
        var locationPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        var bounds = window.getBounds();
        locationPoint.setLocation(locationPoint.x - bounds.width / 2, locationPoint.y - bounds.height / 2);
        window.setLocation(locationPoint);
    }

    public static void show(JFrame frame) {
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void setTitle(JFrame frame, String title) {
        frame.setTitle(title);
    }

    public static void exitOnClose(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void setLoc(Window frame, TGS_ShapeRectangle<Integer> rect) {
        frame.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    public static void setBorder(JFrame frame) {
        frame.getRootPane().setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
    }

    public static void setfavIcon(Window frame, Image image) {
        frame.setIconImage(image);
    }

    public static void setContent(JFrame frame, Container content) {
        frame.setContentPane(content);
    }

    public static void initUnDecorated(JFrame frame) {
        frame.setUndecorated(true);
    }

    public static void setBackgroundTransparentBlack(JFrame frame) {
        frame.setBackground(new Color(100, 100, 100, 50));
    }

    public static void setUnDecoratedTransparent(JFrame frame) {
        frame.setBackground(new Color(0, 0, 0, 0));
    }

    public static Rectangle getUnDecoratedRectangleWithoutMenubar(JFrame frame) {
        var menuBar = frame.getJMenuBar();
        return new Rectangle(frame.getX() + 2, frame.getY() + 2 + menuBar.getHeight(), frame.getWidth() - 4, frame.getHeight() - 4 - menuBar.getHeight());
    }

    public static void setTitleSizeCenterWithMenuBar(JFrame frame, String title, JMenuBar menuBar) {
        frame.setTitle(title);
        frame.setJMenuBar(menuBar);
        frame.setSize(500, 500 + menuBar.getHeight());
        frame.setLocationRelativeTo(null);
    }

    public static void setBorderRed(JFrame frame) {
        frame.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.RED));
    }

    public static void showAlwaysInTop(JFrame frame, boolean alwaysOnTop) {
        frame.setAlwaysOnTop(alwaysOnTop);
        frame.setVisible(true);
    }
}
