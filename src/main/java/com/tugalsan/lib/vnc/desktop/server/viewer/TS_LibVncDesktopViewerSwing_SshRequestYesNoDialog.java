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

import module java.desktop;
import module java.logging;
import java.lang.reflect.*;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewerSwing_SshRequestYesNoDialog {

    private final Component parent;
    private final String title;
    private final String message;

    public TS_LibVncDesktopViewerSwing_SshRequestYesNoDialog(Component parent, String title, String message) {
        this.parent = parent;
        this.message = message;
        this.title = title;
    }

    public boolean ask() {
        var result = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                result[0] = JOptionPane.showConfirmDialog(parent, message, title,
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            Logger.getLogger(this.getClass().getName()).severe(e.getMessage());
        }
        return JOptionPane.YES_OPTION == result[0];
    }

}
