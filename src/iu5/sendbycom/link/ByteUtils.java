package iu5.sendbycom.link;

import java.nio.ByteBuffer;
import java.util.Random;

public class ByteUtils {
    private static Random random = new Random();

    public static byte[] randomHash() {
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public static byte[] intToBytes(int x) {
        return new byte[] { (byte)(x >>> 24), (byte)(x >>> 16), (byte)(x >>> 8), (byte)x };
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }
}
