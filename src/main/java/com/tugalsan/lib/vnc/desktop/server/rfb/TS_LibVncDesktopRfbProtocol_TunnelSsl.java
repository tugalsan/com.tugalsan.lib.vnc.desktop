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

import com.tugalsan.lib.vnc.desktop.server.exceptions.TS_LibVncDesktopException_Transport;
import com.tugalsan.lib.vnc.desktop.server.core.TS_LibVncDesktopTransport_Transport;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @author dime at glavsoft.com
 */
public class TS_LibVncDesktopRfbProtocol_TunnelSsl implements TS_LibVncDesktopRfbProtocol_TunnelHandler {

    /**
     * Requested protocol (algorithm) name. May be one of the follows: SSL
     * Supports some version of SSL; may support other versions SSLv2 Supports
     * SSL version 2 or later; may support other versions SSLv3 Supports SSL
     * version 3; may support other versions TLS Supports some version of TLS;
     * may support other versions TLSv1 Supports RFC 2246: TLS version 1.0 ; may
     * support other versions TLSv1.1 Supports RFC 4346: TLS version 1.1 ; may
     * support other versions TLSv1.2 Supports RFC 5246: TLS version 1.2 ; may
     * support other versions
     *
     * Using TLSv1.1 because earlier versions have vulnerabilities.
     */
    private static final String PROTOCOL = "TLSv1.1";
    private static final String SSL_TRANSPORT = "com.glavsoft.transport.SslTransport";

    public TS_LibVncDesktopRfbProtocol_TunnelSsl() {
    }

    @Override
    public int getId() {
        return TS_LibVncDesktopRfbProtocol_TunnelType.SSL.code;
    }

    @Override
    public TS_LibVncDesktopTransport_Transport createTunnel(TS_LibVncDesktopTransport_Transport transport) throws TS_LibVncDesktopException_Transport {
        try {
            var sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, getTrustAllCertsManager(), null);
            var engine = sslContext.createSSLEngine();
            engine.setUseClientMode(true);
            @SuppressWarnings("unchecked")
            var sslTransportClass = (Class<TS_LibVncDesktopTransport_Transport>) Class.forName(SSL_TRANSPORT);
            var constructor = sslTransportClass.getConstructor(TS_LibVncDesktopTransport_Transport.class, SSLEngine.class);
            return constructor.newInstance(transport, engine);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot create SSL/TLS tunnel", e);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot create SSL/TLS tunnel, SSL transport plugin unavailable", e);
        }

    }

    private TrustManager[] getTrustAllCertsManager() {
        return new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
    }

    public static boolean isTransportAvailable() {
        try {
            Class.forName("com.glavsoft.transport.SslTransport");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
