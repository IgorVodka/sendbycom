package iu5.sendbycom.link.bufferized;

import iu5.sendbycom.application.CommonParams;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.PortClosedException;

import java.util.*;

// todo link level
public class BufferizedSender {
    private List<BufferizedSending> sendings;

    private int maxReceivedIndex;
    private Port port;

    public BufferizedSender(Port port) {
        this.sendings = new ArrayList<>();

        this.port = port;
        this.maxReceivedIndex = -1;
    }

    public void addFrame(Frame frame) {
        frame.setIndex(sendings.size());
        sendings.add(new BufferizedSending(frame));
    }

    public void addFrames(List<Frame> framesToAdd) {
        framesToAdd.forEach(this::addFrame);
    }

    public void tryToSendFrames() throws PortClosedException {
        int maxSendingsIndex = Math.min(sendings.size(), maxReceivedIndex + CommonParams.MAX_PENDING_FRAMES);

        for (int i = Math.max(0, maxReceivedIndex + 1); i < maxSendingsIndex; i++) {
            BufferizedSending sending = sendings.get(i);

            if (sending == null) {
                // todo
                System.out.println("Anomaly. Threading issue?");
                continue;
            }

            if (sending.shouldBeSent()) {
                System.out.println("Notifying as attempted to send: (local) #" + i);
                sending.notifyAttempted();
                port.sendFrame(sending.getFrame());
            }
        }
    }

    public void markAllUpToLocalIndexAsReceivedRemotely(int index) {
        System.out.println("Index: " + index + ", maxReceivedIndex: " + maxReceivedIndex);

        if (index > maxReceivedIndex) {
            for (int i = maxReceivedIndex + 1; i < index + 1; i++) {
                sendings.get(i).markAsReceived();
                System.out.println("Notifying as received on the other side: (local) #" + i);
            }

            this.maxReceivedIndex = index;
        }
    }
}
