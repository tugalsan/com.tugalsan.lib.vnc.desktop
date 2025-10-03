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
package com.tugalsan.lib.vnc.desktop.server.base;

import module com.tugalsan.api.thread;
import com.tugalsan.lib.vnc.desktop.server.exceptions.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;

/**
 * @author dime
 */
public class TS_LibVncDesktopTransport_Transport {

    public final static Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public final static Charset UTF8 = Charset.forName("UTF-8");
    public final TS_ThreadSyncTrigger killTrigger;
    private DataInputStream is;
    private DataOutputStream os;
    private InputStream origIs;
    private OutputStream origOs;
    private TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter;

    public TS_LibVncDesktopTransport_Transport(TS_ThreadSyncTrigger threadKiller) {
        this(threadKiller, null, null);
    }

    public TS_LibVncDesktopTransport_Transport(TS_ThreadSyncTrigger killTrigger, Socket socket) throws IOException {
        this(killTrigger, socket.getInputStream(), socket.getOutputStream());
    }

    public TS_LibVncDesktopTransport_Transport(TS_ThreadSyncTrigger killTrigger, InputStream is) {
        this(killTrigger, is, null);
    }

    public TS_LibVncDesktopTransport_Transport(TS_ThreadSyncTrigger killTrigger, OutputStream os) {
        this(killTrigger, null, os);
    }

    public TS_LibVncDesktopTransport_Transport(TS_ThreadSyncTrigger killTrigger, InputStream is, OutputStream os) {
        this.killTrigger = killTrigger;
        origIs = is;
        this.is = is != null ? new DataInputStream(is) : null;
        origOs = os;
        this.os = os != null ? new DataOutputStream(os) : null;
    }


//    void release() {
//        origIs = is = null;
//        origOs = os = null;
//    }
    public byte readByte() throws TS_LibVncDesktopException_Transport {
        try {
            if (baudrateMeter != null) {
                baudrateMeter.count(1);
            }
            return is.readByte();
        } catch (EOFException e) {
            throw new TS_LibVncDesktopException_ClosedConnection(e);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot read byte", e);
        }

    }

    public void setBaudrateMeter(TS_LibVncDesktopTransport_BaudrateMeter baudrateMeter) {
        this.baudrateMeter = baudrateMeter;
    }

    public int readUInt8() throws TS_LibVncDesktopException_Transport {
        return readByte() & 0x0ff;
    }

    public int readUInt16() throws TS_LibVncDesktopException_Transport {
        return readInt16() & 0x0ffff;
    }

    public short readInt16() throws TS_LibVncDesktopException_Transport {
        try {
            if (baudrateMeter != null) {
                baudrateMeter.count(2);
            }
            return is.readShort();
        } catch (EOFException e) {
            throw new TS_LibVncDesktopException_ClosedConnection(e);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot read int16", e);
        }
    }

    public long readUInt32() throws TS_LibVncDesktopException_Transport {
        return readInt32() & 0xffffffffL;
    }

    public int readInt32() throws TS_LibVncDesktopException_Transport {
        try {
            if (baudrateMeter != null) {
                baudrateMeter.count(4);
            }
            return is.readInt();
        } catch (EOFException e) {
            throw new TS_LibVncDesktopException_ClosedConnection(e);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot read int32", e);
        }
    }

    public long readInt64() throws TS_LibVncDesktopException_Transport {
        try {
            if (baudrateMeter != null) {
                baudrateMeter.count(8);
            }
            return is.readLong();
        } catch (EOFException e) {
            throw new TS_LibVncDesktopException_ClosedConnection(e);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot read int32", e);
        }
    }

    /**
     * Read string by it length. Use this method only when sure no character
     * accept ASCII will be read. Use readBytes and character encoding
     * conversion instead.
     *
     * @return String read
     */
    public String readString(int length) throws TS_LibVncDesktopException_Transport {
//        return new String(readBytes(length), ISO_8859_1);
        return stringWithBytesAndCharset(readBytes(length));
    }

    /**
     * Read 32-bit string length and then string themself by it length Use this
     * method only when sure no character accept ASCII will be read. Use
     * readBytes and character encoding conversion instead or
     * {@link #readUtf8String} method when utf-8 encoding needed.
     *
     * @return String read
     * @throws TS_LibVncDesktopException_Transport
     */
    public String readString() throws TS_LibVncDesktopException_Transport {
        // unset most significant (sign) bit 'cause InputStream#readFully reads
        // [int] length bytes from stream. Change when really need read string more
        // than 2147483647 bytes length
        var length = readInt32() & Integer.MAX_VALUE;
        return readString(length);
    }

    /**
     * Read 32-bit string length and then string themself by it length Assume
     * UTF-8 character encoding used
     *
     * @return String read
     * @throws TS_LibVncDesktopException_Transport
     */
    public String readUtf8String() throws TS_LibVncDesktopException_Transport {
        // unset most significant (sign) bit 'cause InputStream#readFully  reads
        // [int] length bytes from stream. Change when really need read string more
        // than 2147483647 bytes length
        var length = readInt32() & Integer.MAX_VALUE;
        return new String(readBytes(length), UTF8);
    }

    /**
     * Read @code{length} byte array Create byte array with length of
     * @code{length}, read @code{length} bytes and return the array
     *
     * @param length
     * @return byte array which contains the data read
     * @throws TransportException
     */
    public byte[] readBytes(int length) throws TS_LibVncDesktopException_Transport {
        var b = new byte[length];
        return readBytes(b, 0, length);
    }

    public byte[] readBytes(byte[] b, int offset, int length) throws TS_LibVncDesktopException_Transport {
        try {
            is.readFully(b, offset, length);
            if (baudrateMeter != null) {
                baudrateMeter.count(length);
            }
            return b;
        } catch (EOFException e) {
            throw new TS_LibVncDesktopException_ClosedConnection(e);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot read " + length + " bytes array", e);
        }
    }

    public void skip(int length) throws TS_LibVncDesktopException_Transport {
        try {
            var rest = length;
            do {
                rest -= is.skipBytes(rest);
            } while (rest > 0);
            if (baudrateMeter != null) {
                baudrateMeter.count(length);
            }
        } catch (EOFException e) {
            throw new TS_LibVncDesktopException_ClosedConnection(e);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot skip " + length + " bytes", e);
        }
    }

    private void checkForOutputInit() throws TS_LibVncDesktopException_Transport {
        if (null == os) {
            throw new TS_LibVncDesktopException_Transport("Uninitialized writer");
        }
    }

    public TS_LibVncDesktopTransport_Transport flush() throws TS_LibVncDesktopException_Transport {
        checkForOutputInit();
        try {
            os.flush();
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot flush output stream", e);
        }
        return this;
    }

    public TS_LibVncDesktopTransport_Transport writeByte(int b) throws TS_LibVncDesktopException_Transport {
        return write((byte) (b & 0xff));
    }

    public TS_LibVncDesktopTransport_Transport write(byte b) throws TS_LibVncDesktopException_Transport {
        checkForOutputInit();
        try {
            os.writeByte(b);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot write byte", e);
        }
        return this;
    }

    public TS_LibVncDesktopTransport_Transport writeInt16(int sh) throws TS_LibVncDesktopException_Transport {
        return write((short) (sh & 0xffff));
    }

    public TS_LibVncDesktopTransport_Transport write(short sh) throws TS_LibVncDesktopException_Transport {
        checkForOutputInit();
        try {
            os.writeShort(sh);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot write short", e);
        }
        return this;
    }

    public TS_LibVncDesktopTransport_Transport writeInt32(int i) throws TS_LibVncDesktopException_Transport {
        return write(i);
    }

    public TS_LibVncDesktopTransport_Transport writeUInt32(long i) throws TS_LibVncDesktopException_Transport {
        return write((int) i & 0xffffffff);
    }

    public TS_LibVncDesktopTransport_Transport writeInt64(long i) throws TS_LibVncDesktopException_Transport {
        checkForOutputInit();
        try {
            os.writeLong(i);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot write long", e);
        }
        return this;
    }

    public TS_LibVncDesktopTransport_Transport write(int i) throws TS_LibVncDesktopException_Transport {
        checkForOutputInit();
        try {
            os.writeInt(i);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot write int", e);
        }
        return this;
    }

    public TS_LibVncDesktopTransport_Transport write(byte[] b) throws TS_LibVncDesktopException_Transport {
        return write(b, 0, b.length);
    }

    public TS_LibVncDesktopTransport_Transport write(byte[] b, int length) throws TS_LibVncDesktopException_Transport {
        return write(b, 0, length);
    }

    public TS_LibVncDesktopTransport_Transport write(byte[] b, int offset, int length) throws TS_LibVncDesktopException_Transport {
        checkForOutputInit();
        try {
            os.write(b, offset, length <= b.length ? length : b.length);
        } catch (IOException e) {
            throw new TS_LibVncDesktopException_Transport("Cannot write " + length + " bytes", e);
        }
        return this;
    }

    public void setOutputStreamTo(OutputStream os) {
        this.os = new DataOutputStream(os);
    }

    public TS_LibVncDesktopTransport_Transport zero(int count) throws TS_LibVncDesktopException_Transport {
        while (count-- > 0) {
            writeByte(0);
        }
        return this;
    }

    private String stringWithBytesAndCharset(byte[] bytes) {
        String result;
        try {
            result = new String(bytes, ISO_8859_1);
        } catch (NoSuchMethodError error) {
            try {
                result = new String(bytes, ISO_8859_1.name());
            } catch (UnsupportedEncodingException e) {
                result = null;
            }
        }
        return result;
    }
}
