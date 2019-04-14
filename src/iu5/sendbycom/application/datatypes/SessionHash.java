package iu5.sendbycom.application.datatypes;

import iu5.sendbycom.link.ByteUtils;

import java.util.Arrays;

public class SessionHash {
    private byte[] bytes;

    public SessionHash(byte[] bytes) {
        this.bytes = bytes;
    }

    public static SessionHash generate() {
        return new SessionHash(ByteUtils.randomHash());
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof SessionHash) {
            return Arrays.equals(bytes, ((SessionHash) another).getBytes());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
