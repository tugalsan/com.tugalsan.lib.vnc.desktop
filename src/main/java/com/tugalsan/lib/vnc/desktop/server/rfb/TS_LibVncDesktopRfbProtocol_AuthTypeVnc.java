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

import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Crypto;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Fatal;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.rfb.TS_LibVncDesktopRfbProtocol_Protocol;
import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopTransport_Transport;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopUtils_Strings.getBytesWithCharset;

public class TS_LibVncDesktopRfbProtocol_AuthTypeVnc extends TS_LibVncDesktopRfbProtocol_AuthHandler {

    @Override
    public TS_LibVncDesktopRfbProtocol_AuthTypes getType() {
        return TS_LibVncDesktopRfbProtocol_AuthTypes.VNC_AUTHENTICATION;
    }

    @Override
    public TS_LibVncDesktopTransport_Transport authenticate(TS_LibVncDesktopTransport_Transport transport, TS_LibVncDesktopRfbProtocol_Protocol protocol)
            throws TS_LibVncDesktopException_Transport, TS_LibVncDesktopException_Fatal {
        var challenge = transport.readBytes(16);
        var password = protocol.getPasswordRetriever().getResult();
        if (null == password) {
            password = "";
        }
        var key = new byte[8];
        System.arraycopy(getBytesWithCharset(password, TS_LibVncDesktopTransport_Transport.ISO_8859_1), 0, key, 0, Math.min(key.length, getBytesWithCharset(password, TS_LibVncDesktopTransport_Transport.ISO_8859_1).length));
        transport.write(encrypt(challenge, key)).flush();
        return transport;
    }

    /**
     * Encrypt challenge by key using DES
     *
     * @return encrypted bytes
     * @throws TS_LibVncDesktopException_Crypto on problem with DES algorithm support or smth
     * about
     */
    public byte[] encrypt(byte[] challenge, byte[] key) throws TS_LibVncDesktopException_Crypto {
        try {
            var desKeySpec = new DESKeySpec(mirrorBits(key));
            var keyFactory = SecretKeyFactory.getInstance("DES");
            var secretKey = keyFactory.generateSecret(desKeySpec);
            var desCipher = Cipher.getInstance("DES/ECB/NoPadding");
            desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return desCipher.doFinal(challenge);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e) {
            throw new TS_LibVncDesktopException_Crypto("Cannot encrypt challenge", e);
        }
    }

    private byte[] mirrorBits(byte[] k) {
        var key = new byte[8];
        for (var i = 0; i < 8; i++) {
            var s = k[i];
            s = (byte) (((s >> 1) & 0x55) | ((s << 1) & 0xaa));
            s = (byte) (((s >> 2) & 0x33) | ((s << 2) & 0xcc));
            s = (byte) (((s >> 4) & 0x0f) | ((s << 4) & 0xf0));
            key[i] = s;
        }
        return key;
    }

}
