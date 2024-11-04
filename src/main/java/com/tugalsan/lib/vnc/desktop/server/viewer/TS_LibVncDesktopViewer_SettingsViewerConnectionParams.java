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

import com.tugalsan.lib.vnc.desktop.server.base.TS_LibVncDesktopUtils_Strings;
import java.util.Objects;
import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_MvpModel;

/**
 * Object that represents parameters needed for establishing network connection
 * to remote host. This is used to pass a number of parameters into connection
 * establishing module and to provide a Connection History interface feature.
 *
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewer_SettingsViewerConnectionParams implements TS_LibVncDesktopViewer_MvpModel {

    public static final int DEFAULT_RFB_PORT = 5900;

    /**
     * A name of remote host. Rather symbolic (dns) name or ip address Ex.
     * remote.host.mydomain.com or localhost or 192.168.0.2 etc.
     */
    public String hostName;
    /**
     * A port number of remote host. Default is 5900
     */
    private int portNumber;

    public TS_LibVncDesktopViewer_SettingsViewerConnectionParams(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public TS_LibVncDesktopViewer_SettingsViewerConnectionParams(TS_LibVncDesktopViewer_SettingsViewerConnectionParams cp) {
        this.hostName = cp.hostName != null ? cp.hostName : "";
        this.portNumber = cp.portNumber;
    }

    public TS_LibVncDesktopViewer_SettingsViewerConnectionParams() {
        hostName = "";
    }

    public TS_LibVncDesktopViewer_SettingsViewerConnectionParams(String hostName, String portNumber) {
        this.hostName = hostName;
        try {
            setPortNumber(portNumber);
        } catch (TS_LibVncDesktopViewer_SettingsWrongParameterException ignore) {
            // use default
            this.portNumber = 0;
        }
    }

    /**
     * Check host name empty
     *
     * @return true if host name is empty
     */
    public boolean isHostNameEmpty() {
        return TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(hostName);
    }

    /**
     * Parse port number from string specified. Thrown WrongParameterException
     * on error.
     *
     * @param port string representation of port number
     * @throws TS_LibVncDesktopViewer_SettingsWrongParameterException when parsing error occurs or port number
     * is out of range
     * @return portNubmer parsed
     */
    private int parsePortNumber(String port) throws TS_LibVncDesktopViewer_SettingsWrongParameterException {
        int _portNumber;
        if (null == port) {
            return 0;
        }
        try {
            _portNumber = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            _portNumber = 0;
            if (!TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(port)) {
                throw new TS_LibVncDesktopViewer_SettingsWrongParameterException("Wrong port number: " + port + "\nMust be in 0..65535");
            }
        }
        if (_portNumber > 65535 || _portNumber < 0) {
            throw new TS_LibVncDesktopViewer_SettingsWrongParameterException("Port number is out of range: " + port + "\nMust be in 0..65535");
        }
        return _portNumber;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return this.hostName;
    }

    /**
     * Parse port number from string specified. Thrown WrongParameterException
     * on error. Set portNumber property when port on success. throws
     * WrongParameterException when parsing error occurs or port number is out
     * of range
     *
     * @param port string representation of port number
     */
    final public void setPortNumber(String port) throws TS_LibVncDesktopViewer_SettingsWrongParameterException {
        portNumber = this.parsePortNumber(port);
    }

    public void setPortNumber(int port) {
        this.portNumber = port;
    }

    public int getPortNumber() {
        return 0 == portNumber ? DEFAULT_RFB_PORT : portNumber;
    }

    /**
     * Copy and complete only field that are empty (null, zerro or empty string)
     * in `this' object from the other one
     *
     * @param other ConnectionParams object to copy fields from
     */
    public void completeEmptyFieldsFrom(TS_LibVncDesktopViewer_SettingsViewerConnectionParams other) {
        if (null == other) {
            return;
        }
        if (TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(hostName) && !TS_LibVncDesktopUtils_Strings.isTrimmedEmpty(other.hostName)) {
            hostName = other.hostName;
        }
        if (0 == portNumber && other.portNumber != 0) {
            portNumber = other.portNumber;
        }
    }

    @Override
    public String toString() {
        return hostName != null ? hostName : "";
//        return (hostName != null ? hostName : "") + ":" + portNumber + " " + useSsh + " " + sshUserName + "@" + sshHostName + ":" + sshPortNumber;
    }

    /**
     * For logging purpose
     *
     * @return string representation of object
     */
    public String toPrint() {
        return "ConnectionParams{"
                + "hostName='" + hostName + '\''
                + ", portNumber=" + portNumber
                + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof TS_LibVncDesktopViewer_SettingsViewerConnectionParams)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        var o = (TS_LibVncDesktopViewer_SettingsViewerConnectionParams) obj;
        return isEqualsNullable(hostName, o.hostName) && getPortNumber() == o.getPortNumber();
    }

    private boolean isEqualsNullable(String one, String another) {
        return (null == one ? "" : one).equals(null == another ? "" : another);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                hostName != null ? hostName.hashCode() : 0,
                portNumber
        );
    }

    public void clearFields() {
        hostName = "";
        portNumber = 0;
    }
}
