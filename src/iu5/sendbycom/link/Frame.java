package iu5.sendbycom.link;

import iu5.sendbycom.link.hamming.Hamming;
import iu5.sendbycom.link.hamming.HammingEncoder;
import iu5.sendbycom.link.hamming.HammingReceiver;
import iu5.sendbycom.link.hamming.Message;
import iu5.sendbycom.physical.exception.DataTooBigException;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;

// TODO: hamming!
public class Frame {
    public static final int MAX_DATA_SIZE = 254;

    private FrameType type;
    private byte[] data;
    private int index;

    public Frame(FrameType type, int index, byte[] data) throws DataTooBigException {
        this.type = type;
        this.data = data;
        this.index = index;

        if (data.length > Frame.MAX_DATA_SIZE) {
            throw new DataTooBigException();
        }
    }

    public Frame(FrameType type, String data) throws DataTooBigException {
        this(type, data.getBytes());
    }

    public Frame(FrameType type, byte[] data) throws DataTooBigException {
        this(type, 0, data);
    }

    public byte[] toBytes() {
        byte[] encodedData = Hamming.encode(data);

        byte[] result = new byte[1 + 1 + 4 + encodedData.length];
        result[0] = (byte) type.ordinal();
        result[1] = (byte) encodedData.length;

        System.arraycopy(ByteUtils.intToBytes(index), 0, result, 2, 4);
        System.arraycopy(encodedData, 0, result, 6, encodedData.length);

        return result;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public FrameType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }
}
