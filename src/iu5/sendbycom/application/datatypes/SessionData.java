package iu5.sendbycom.application.datatypes;

import java.util.Arrays;

public class SessionData {
    private SessionHash hash;
    private byte[] body;

    public SessionData(SessionHash hash, byte[] body) {
        this.hash = hash;
        this.body = body;
    }

    public SessionData(SessionHash hash, String str) {
        this(hash, str.getBytes());
    }

    public byte[] encode() {
        byte[] result = Arrays.copyOf(hash.getBytes(), hash.getBytes().length + body.length);
        System.arraycopy(body, 0, result, hash.getBytes().length, body.length);
        return result;
    }

    public static SessionData decode(byte[] hashAndBytes) {
        assert hashAndBytes.length >= 4;

        byte[] hash = new byte[4];
        byte[] bytes = new byte[hashAndBytes.length - 4];
        System.arraycopy(hashAndBytes, 0, hash, 0, 4);
        System.arraycopy(hashAndBytes, 4, bytes, 0, hashAndBytes.length - 4);

        return new SessionData(new SessionHash(hash), bytes);
    }

    public SessionHash getHash() {
        return hash;
    }

    public byte[] getBody() {
        return body;
    }
}
