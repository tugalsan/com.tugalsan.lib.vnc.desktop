package com.tugalsan.api.stream.server;

import com.tugalsan.api.unsafe.client.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class TS_StreamUtils {

    public static void transfer(InputStream src0, OutputStream dest0) {
        TGS_UnSafe.run(() -> {
            try ( var src = src0;  var dest = dest0;  var inputChannel = Channels.newChannel(src);  var outputChannel = Channels.newChannel(dest);) {
                transfer(inputChannel, outputChannel);
            }
        });
    }

    public static void transfer(ReadableByteChannel src0, WritableByteChannel dest0) {
        TGS_UnSafe.run(() -> {
            try ( var src = src0;  var dest = dest0;) {
                var buffer = ByteBuffer.allocateDirect(16 * 1024);
                while (src.read(buffer) != -1) {
                    buffer.flip();
                    dest.write(buffer);
                    buffer.compact();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    dest.write(buffer);
                }
            }
        });
    }

    public static int readInt(InputStream is0) {
        return TGS_UnSafe.call(() -> {
            try ( var is = is0) {
                var byte_array_4 = new byte[4];
                byte_array_4[0] = (byte) is.read();
                byte_array_4[1] = (byte) is.read();
                byte_array_4[2] = (byte) is.read();
                byte_array_4[3] = (byte) is.read();
                return ByteBuffer.wrap(byte_array_4).getInt();
            }
        });
    }
}
