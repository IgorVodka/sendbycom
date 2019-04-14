package iu5.sendbycom.link;

import iu5.sendbycom.link.hamming.Hamming;
import iu5.sendbycom.link.hamming.HammingReceiver;
import iu5.sendbycom.link.hamming.Message;
import iu5.sendbycom.physical.exception.DataTooBigException;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.PortClosedException;

import java.lang.reflect.Array;
import java.util.BitSet;

public class FrameReceiver {
    private Port port;

    public FrameReceiver(Port port) {
        this.port = port;
    }

    public Frame nextFrame() throws PortClosedException {
        try {
            byte[] buffer = new byte[Frame.MAX_DATA_SIZE];

            port.readBytes(buffer, 1);
            Thread.sleep(3); // todo: hack not to receive two same bytes

            FrameType frameType = FrameType.fromByte(buffer[0]);

            System.out.println("Read frame of type: " + frameType.name());

            if (frameType == FrameType.BROKEN || frameType == FrameType.UNKNOWN) {
                // Okay, this frame is broken.
                return new Frame(FrameType.BROKEN, new byte[]{});
            }

            port.readBytes(buffer, 1);
            Thread.sleep(3); // todo: hack not to receive two same bytes

            short length = buffer[0];

            if (length < 0) {
                // fix signed bytes
                length += 256;
                System.out.println("Got a packet with length < 256: fixing length to be " + length);
            }

            port.readBytes(buffer, 4);
            Thread.sleep(5); // todo: hack not to receive two same bytes

            byte[] firstFourBytes = new byte[] { buffer[0], buffer[1], buffer[2], buffer[3] };
            int index = ByteUtils.bytesToInt(firstFourBytes);
            System.out.println("PARSED INDEX: " + index);

            byte[] data;

            if (length < 0) {
                // Length is less than 0! Impossible. TODO exc
                return new Frame(FrameType.BROKEN, new byte[]{});
            } else if (length > 0) {
                int receivingPosition = 0;

                //System.out.println("Going to receive " + length + " bytes. Receiving position = 0.");
                data = new byte[length];

                while (receivingPosition < length) {
                    int bytesAvailable = Math.min(port.bytesAvailable(), length - receivingPosition);

                    if (bytesAvailable > 0) {
                        //System.out.println("Available bytes: " + bytesAvailable + ", recv pos = " + receivingPosition);
                        port.readBytes(buffer, bytesAvailable);
                        System.arraycopy(buffer, 0, data, receivingPosition, bytesAvailable);
                        receivingPosition += bytesAvailable;
                    } else {
                        //System.out.println("No bytes available. Retrying...");
                        Thread.sleep(1); // todo replace with a smart buffer
                    }
                }
            } else {
                data = new byte[0];
            }

            return new Frame(frameType, index, Hamming.decode(data));
        } catch (DataTooBigException e) {
            // Won't happen as length is read from a byte
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasData() {
        return port.bytesAvailable() > 0;
    }
}
