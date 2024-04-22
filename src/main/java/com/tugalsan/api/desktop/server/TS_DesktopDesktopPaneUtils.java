package com.tugalsan.api.desktop.server;

import com.tugalsan.api.shape.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.awt.*;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;

public class TS_DesktopDesktopPaneUtils {

    public static void remove(JDesktopPane desktopPane, Component comp) {
        desktopPane.remove(comp);
    }

    public static void tiltWindows(JDesktopPane desktopPane) {
        var visibleFrames = Arrays.stream(desktopPane.getAllFrames())
                .filter(f -> f.isVisible())
                .collect(Collectors.toCollection(ArrayList::new));
        if (visibleFrames.isEmpty()) {
            return;
        }
        // Determine the necessary grid size
        var count = visibleFrames.size();
        var sqrt = (int) Math.sqrt(count);
        var wrap = new Object() {
            int rows = sqrt;
            int cols = sqrt;
        };
        if (wrap.rows * wrap.cols < count) {
            wrap.cols++;
            if (wrap.rows * wrap.cols < count) {
                wrap.rows++;
            }
        }
        // Define some initial values for size & location.
        var size = desktopPane.getSize();
        TGS_ShapeRectangle<Integer> s = new TGS_ShapeRectangle(0, 0, size.width / wrap.cols, size.height / wrap.rows);
        // Iterate over the frames, deiconifying any iconified frames and then
        // relocating & resizing each.
        IntStream.range(0, wrap.rows).forEachOrdered(i -> {
            for (var j = 0; j < wrap.cols && ((i * wrap.cols) + j < count); j++) {
                var f = visibleFrames.get((i * wrap.cols) + j);
                if (!f.isClosed() && f.isIcon()) {
                    TGS_UnSafe.run(() -> f.setIcon(false), e -> TGS_UnSafe.runNothing());
                }
                desktopPane.getDesktopManager().resizeFrame(f, s.x, s.y, s.width, s.height);
                s.x += s.width;
            }
            s.y += s.height; // start the next row
            s.x = 0;
        });
    }

    public static void paintComponent(JDesktopPane pane, Graphics g, Image imgBack) {
        if (imgBack != null) {
            var x = (pane.getWidth() - imgBack.getWidth(null)) / 2;
            var y = (pane.getHeight() - imgBack.getHeight(null)) / 2;
            g.drawImage(imgBack, x, y, pane);
        }
    }

    public static void keepInternalFramesInsideThePane(JDesktopPane pane) {
        pane.setDesktopManager(new DefaultDesktopManager() {
            // This is called anytime a frame is moved. This
            // implementation keeps the frame from leaving the desktop.
            @Override
            public void dragFrame(JComponent f, int x, int y) {
                if (f instanceof JInternalFrame frame) { // Deal only w/internal frames
                    var desk = frame.getDesktopPane();
                    var d = desk.getSize();
                    // Nothing all that fancy below, just figuring out how to adjust
                    // to keep the frame on the desktop.
                    if (x < 0) { // too far left?
                        x = 0; // flush against the left side
                    } else {
                        if (x + frame.getWidth() > d.width) { // too far right?
                            x = d.width - frame.getWidth(); // flush against right side
                        }
                    }
                    if (y < 0) { // too high?
                        y = 0; // flush against the top
                    } else {
                        if (y + frame.getHeight() > d.height) { // too low?
                            y = d.height - frame.getHeight(); // flush against the
                            // bottom
                        }
                    }
                }
                // Pass along the (possibly cropped) values to the normal drag handler.
                super.dragFrame(f, x, y);
            }
        });
    }
}
