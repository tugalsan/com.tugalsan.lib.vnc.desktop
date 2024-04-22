//https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-os/src/main/java/com/baeldung/example/soundapi/WaveDataUtil.java
package com.tugalsan.api.input.server;

import com.tugalsan.api.coronator.client.*;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.TS_ThreadAsync;
import com.tugalsan.api.unsafe.client.*;
import java.io.*;
import java.nio.file.*;
import javax.sound.sampled.*;

public class TS_InputSound {

    public static TS_InputSound of(TS_ThreadSyncTrigger killTrigger, Path file) {
        return new TS_InputSound(killTrigger, file);
    }

    public TS_InputSound(TS_ThreadSyncTrigger killTrigger, Path file) {
        this.file = file;
        format = TGS_Coronator.of(AudioFormat.class).coronateAs(val -> {
            var encoding = AudioFormat.Encoding.PCM_SIGNED; 
            var rate = 44100.0f;
            int channels = 2;
            var sampleSize = 16;
            var bigEndian = true;
            return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
        });
        TS_ThreadAsync.now(killTrigger, kt -> {
            TGS_UnSafe.run(() -> {
                try (var out = new ByteArrayOutputStream(); var line = getTargetDataLineForRecord();) {
                    var frameSizeInBytes = format.getFrameSize();
                    var bufferLengthInFrames = line.getBufferSize() / 8;
                    var bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
                    pumpByteOutputStream(killTrigger, out, line, bufferLengthInBytes);
                    audioInputStream = new AudioInputStream(line);
                    audioInputStream = convertToAudioIStream(out, frameSizeInBytes);
                    audioInputStream.reset();
                }
            });
        });
    }
    private Path file;
    private AudioFormat format;
    private AudioInputStream audioInputStream;
    private boolean kill = false;

    public TS_InputSound kill() {
        kill = true;
        return this;
    }

    private void pumpByteOutputStream(TS_ThreadSyncTrigger killTrigger, ByteArrayOutputStream out, TargetDataLine line, int bufferLengthInBytes) {
        var data = new byte[bufferLengthInBytes];
        var numBytesRead = 0;
        line.start();
        while (!kill) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, 0, numBytesRead);
            if (killTrigger.hasTriggered()) {
                kill = true;
            }
        }
    }

    private AudioInputStream convertToAudioIStream(ByteArrayOutputStream out, int frameSizeInBytes) {
        var audioBytes = out.toByteArray();
        var bais = new ByteArrayInputStream(audioBytes);
        var audioStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
        var milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
        var duration = milliseconds / 1000.0;
        System.out.println("Recorded duration in seconds:" + duration);
        return audioStream;
    }

    private TargetDataLine getTargetDataLineForRecord() {
        return TGS_UnSafe.call(() -> {
            var info = new DataLine.Info(TargetDataLine.class, format);
            System.out.println("line.info: " + info.toString());
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("line not supported: " + info.toString());
                return null;
            }
            var line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
            return line;
        }, e -> {
            e.printStackTrace();
            return null;
        });
    }

    public boolean saveToFile() {
        return TGS_UnSafe.call(() -> {
            var fileType = AudioFileFormat.Type.WAVE;
            System.out.println("Saving...");
            if (null == fileType || audioInputStream == null) {
                return false;
            }
            var myFile = file.toFile();
            audioInputStream.reset();
            int i = 0;
            while (myFile.exists()) {
                String temp = "" + i + myFile.getName();
                myFile = new File(temp);
            }
            AudioSystem.write(audioInputStream, fileType, myFile);
            System.out.println("Saved " + myFile.getAbsolutePath());
            return true;
        }, e -> {
            e.printStackTrace();
            return false;
        });
    }
}
