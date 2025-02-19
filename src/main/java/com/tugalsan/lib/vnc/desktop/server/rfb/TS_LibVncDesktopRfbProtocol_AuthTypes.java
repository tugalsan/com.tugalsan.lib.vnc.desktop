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

/**
 * Security types that implemented
 */
public enum TS_LibVncDesktopRfbProtocol_AuthTypes {
    NONE_AUTHENTICATION(1),
    VNC_AUTHENTICATION(2),
    //	int RA2_AUTHENTICATION = 5;
    //	int RA2NE_AUTHENTICATION = 6;
    TIGHT_AUTHENTICATION(16),
    TIGHT2_AUTHENTICATION(116);
//	int ULTRA_AUTHENTICATION = 17;
//	int TLS_AUTHENTICATION = 18;
//	int VENCRYPT_AUTHENTICATION = 19;

    private final int id;

    private TS_LibVncDesktopRfbProtocol_AuthTypes(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
