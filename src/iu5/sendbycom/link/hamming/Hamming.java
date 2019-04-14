package iu5.sendbycom.link.hamming;

import java.util.BitSet;

public class Hamming {
    public static byte[] encode(byte[] input) {
        BitSet inputBits = BitSet.valueOf(input);
        HammingEncoder encoder = new HammingEncoder();

        BitSet encodedMsgBits = new BitSet();
        int chunkCount = input.length * 2; // e.g. 2 bytes = 16 bits = 4 chunks by 4 => len * 2

        for (int i = 0; i < chunkCount; i++) {
            BitSet chunk = inputBits.get(i * 4, i * 4 + 4);

            Message encodedMessage = encoder.encode(new Message(chunk), 4);
            BitSet encodedBits = encodedMessage.getBits();

            for (int j = 0; j < 7; j++) {
                encodedMsgBits.set(i * 7 + j, encodedBits.get(j));
            }
        }

        int actualLength = encodedMsgBits.toByteArray().length;
        int expectedLength = (int) Math.ceil(chunkCount * 7 / 8.0);

        byte[] result = new byte[expectedLength];

        System.arraycopy(
                encodedMsgBits.toByteArray(),
                0,
                result,
                0,
                actualLength
        );

        return result;
    }

    public static byte[] decode(byte[] encoded) {
        HammingReceiver receiver = new HammingReceiver();
        BitSet buffer = new BitSet();

        int chunkCount = (int) Math.ceil(encoded.length * 8 / 7.0);
        BitSet encodedMsgBits = BitSet.valueOf(encoded);

        for (int i = 0; i < chunkCount; i++) {
            BitSet encodedChunk = encodedMsgBits.get(i * 7, i * 7 + 7);

            if (encodedChunk.length() >= 0) {
                Message encodedMessage = new Message(encodedChunk);
                int error = receiver.findError(encodedMessage, 7);

                if (error != -1) {
                    System.out.println("ERROR!!! RESEND."); // TODO DON'T FIX ERRORS, INSTEAD THROW AN EXCEPTION
                }

                Message fixedEncodedMessage = receiver.fixBits(encodedMessage, error, 7);
                Message decodedMessage = receiver.selectInformationBits(fixedEncodedMessage, 7);

                for (int j = 0; j < 4; j++) {
                    buffer.set(i * 4 + j, decodedMessage.getBits().get(j));
                }
            }
        }

        int actualLength = buffer.toByteArray().length;
        int expectedLength = (int) Math.floor(chunkCount * 4 / 8.0);

        byte[] result = new byte[expectedLength];

        try {
            System.arraycopy(
                    buffer.toByteArray(),
                    0,
                    result,
                    0,
                    actualLength
            );
        } catch (Exception exc) {
            // oh hax
            return buffer.toByteArray();
        }

        return result;
    }
}
