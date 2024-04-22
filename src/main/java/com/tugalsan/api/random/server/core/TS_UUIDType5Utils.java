package com.tugalsan.api.random.server.core;

import com.tugalsan.api.unsafe.client.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.UUID;

public class TS_UUIDType5Utils {

//    public static void main(String[] args) {
//        System.out.println(TS_UUIDType5Utils.nameUUIDFromNamespaceAndString(NAMESPACE_URL, "google.com"));
//        System.out.println(TS_UUIDType5Utils.nameUUIDFromNamespaceAndString(NAMESPACE_URL, "google.com"));
//        System.out.println(TS_UUIDType5Utils.nameUUIDFromNamespaceAndString(NAMESPACE_URL, "google.com"));
//        var a = run("AA BB 2");
//        System.out.println(a);
//    }
    public static UUID run(CharSequence name) {
        return nameUUIDFromNamespaceAndString(getNAMESPACE_URL(), name);
    }

//    private static final UUID getNAMESPACE_DNS() {
//        return UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
//    }

    private static UUID getNAMESPACE_URL() {
        return UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    }

//    private static final UUID getNAMESPACE_OID() {
//        return UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
//    }
//
//    private static final UUID getNAMESPACE_X500() {
//        return UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");
//    }

    private static UUID nameUUIDFromNamespaceAndString(UUID namespace, CharSequence name) {
        return nameUUIDFromNamespaceAndBytes(namespace, Objects.requireNonNull(name.toString(), "name == null").getBytes(StandardCharsets.UTF_8));
    }

    private static UUID nameUUIDFromNamespaceAndBytes(UUID namespace, byte[] name) {
        return TGS_UnSafe.call(() -> {
            var md = MessageDigest.getInstance("SHA-1");
            md.update(toBytes(Objects.requireNonNull(namespace, "namespace is null")));
            md.update(Objects.requireNonNull(name, "name is null"));
            var sha1Bytes = md.digest();
            sha1Bytes[6] &= 0x0f;//clear version        
            sha1Bytes[6] |= 0x50;// set to version 5    
            sha1Bytes[8] &= 0x3f;// clear variant       
            sha1Bytes[8] |= 0x80;// set to IETF variant 
            return fromBytes(sha1Bytes);
        }, e -> {
            return TGS_UnSafe.thrw(TS_UUIDType5Utils.class.getSimpleName(), "nameUUIDFromNamespaceAndBytes", "SHA-1 not supported");
        });
    }

    private static UUID fromBytes(byte[] data) {
        // Based on the private UUID(bytes[]) constructor
        var msb = 0L;
        var lsb = 0L;
        assert data.length >= 16;
        for (var i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (var i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    private static byte[] toBytes(UUID uuid) {
        // inverted logic of fromBytes()
        var out = new byte[16];
        var msb = uuid.getMostSignificantBits();
        var lsb = uuid.getLeastSignificantBits();
        for (var i = 0; i < 8; i++) {
            out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
        }
        for (var i = 8; i < 16; i++) {
            out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
        }
        return out;
    }
}
