package iu5.sendbycom.link.bufferized;

import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameType;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class BufferizedReceiver {
    private Map<Integer, Frame> frames;

    private int lastReceivedInOrder;

    public BufferizedReceiver() {
        this.frames = new HashMap<>();
        this.lastReceivedInOrder = -1;
    }

    public void addFrame(Frame frame) {
        // expected frame
        int nextOrderedFrame = lastReceivedInOrder + 1;

        if (frame.getIndex() != nextOrderedFrame) {
            // todo maybe don't even store it but only get in order, because
            // todo server actually keeps sending these frames again one by one
            System.out.println("Received unexpected frame... storing it. Expected (ordered): " + nextOrderedFrame);
            System.out.println("    while actual (just received): " + frame.getIndex());
        }

        this.frames.put(frame.getIndex(), frame);
    }

    public Frame getOrderedFrameOrNull() {
        int nextOrderedFrame = lastReceivedInOrder + 1;
        if (frames.containsKey(nextOrderedFrame)) {
            System.out.println("Received a frame in order!");
            lastReceivedInOrder = nextOrderedFrame;
            return frames.get(nextOrderedFrame);
        } else {
            return null;
        }
    }
}
