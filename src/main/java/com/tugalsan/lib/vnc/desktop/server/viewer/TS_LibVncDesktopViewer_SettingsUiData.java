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

import java.io.*;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewer_SettingsUiData implements Serializable {

    private static final long serialVersionUID = 1L;
    private double scalePercent;

    public TS_LibVncDesktopViewer_SettingsUiData() {
        scalePercent = 100;
    }

    public TS_LibVncDesktopViewer_SettingsUiData(double scalePercent) {
        this.scalePercent = scalePercent;
    }

    public TS_LibVncDesktopViewer_SettingsUiData(TS_LibVncDesktopViewer_SettingsUiData other) {
        this(other.getScalePercent());
    }

    public double getScalePercent() {
        return scalePercent;
    }

    public boolean setScalePercent(double scalePercent) {
        if (this.scalePercent != scalePercent) {
            this.scalePercent = scalePercent;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UiSettingsData{"
                + "scalePercent=" + scalePercent
                + '}';
    }
}
