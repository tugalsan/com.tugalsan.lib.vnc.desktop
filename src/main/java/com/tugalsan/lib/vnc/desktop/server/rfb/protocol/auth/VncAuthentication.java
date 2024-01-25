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
package com.tugalsan.lib.vnc.desktop.server.rfb.protocol.auth;

import com.tugalsan.lib.vnc.desktop.server.exceptions.CryptoException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.FatalException;
import com.tugalsan.lib.vnc.desktop.server.exceptions.TransportException;
import com.tugalsan.lib.vnc.desktop.server.rfb.protocol.Protocol;
import com.tugalsan.lib.vnc.desktop.server.transport.Transport;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static com.tugalsan.lib.vnc.desktop.server.utils.Strings.getBytesWithCharset;

public class VncAuthentication extends AuthHandler {

    @Override
    public SecurityType getType() {
        return SecurityType.VNC_AUTHENTICATION;
    }

    @Override
    public Transport authenticate(Transport transport, Protocol protocol)
            throws TransportException, FatalException {
        var challenge = transport.readBytes(16);
        var password = protocol.getPasswordRetriever().getResult();
        if (null == password) {
            password = "";
        }
        var key = new byte[8];
        System.arraycopy(getBytesWithCharset(password, Transport.ISO_8859_1), 0, key, 0, Math.min(key.length, getBytesWithCharset(password, Transport.ISO_8859_1).length));
        transport.write(encrypt(challenge, key)).flush();
        return transport;
    }

    /**
     * Encrypt challenge by key using DES
     *
     * @return encrypted bytes
     * @throws CryptoException on problem with DES algorithm support or smth
     * about
     */
    public byte[] encrypt(byte[] challenge, byte[] key) throws CryptoException {
        try {
            var desKeySpec = new DESKeySpec(mirrorBits(key));
            var keyFactory = SecretKeyFactory.getInstance("DES");
            var secretKey = keyFactory.generateSecret(desKeySpec);
            var desCipher = Cipher.getInstance("DES/ECB/NoPadding");
            desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return desCipher.doFinal(challenge);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e) {
            throw new CryptoException("Cannot encrypt challenge", e);
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
