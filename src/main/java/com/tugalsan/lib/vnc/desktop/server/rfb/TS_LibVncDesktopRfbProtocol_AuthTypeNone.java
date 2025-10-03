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

import com.tugalsan.lib.vnc.desktop.server.exceptions.*;
import com.tugalsan.lib.vnc.desktop.server.base.*;

public class TS_LibVncDesktopRfbProtocol_AuthTypeNone extends TS_LibVncDesktopRfbProtocol_AuthHandler {

    @Override
    public TS_LibVncDesktopTransport_Transport authenticate(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol) throws TS_LibVncDesktopException_Transport {
        return transport;
    }

    @Override
    public TS_LibVncDesktopRfbProtocol_AuthTypes getType() {
        return TS_LibVncDesktopRfbProtocol_AuthTypes.NONE_AUTHENTICATION;
    }

}
