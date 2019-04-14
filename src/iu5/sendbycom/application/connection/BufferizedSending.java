package iu5.sendbycom.application.connection;

import iu5.sendbycom.application.CommonParams;
import iu5.sendbycom.link.Frame;

import java.util.Date;

public class BufferizedSending {
    private Frame frame;
    private BufferizedSendingState state;
    private Long lastSendingAttempt;
    private int attemptsLeft;

    public BufferizedSending(Frame frame) {
        this.frame = frame;
        this.state = BufferizedSendingState.PENDING;
        this.lastSendingAttempt = 0L;
        this.attemptsLeft = 3; // todo: currently not handled in any way!
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean shouldBeSent() {
        // todo if attempts left = -1, throw exception

        return state != BufferizedSendingState.RECEIVED
                && (new Date().getTime() - lastSendingAttempt) > CommonParams.RESEND_TIMEOUT;
    }

    public BufferizedSendingState getState() {
        return state;
    }

    public void notifyAttempted() {
        this.state = BufferizedSendingState.SENT;
        this.lastSendingAttempt = new Date().getTime();
        this.attemptsLeft--;
    }

    public void markAsReceived() {
        this.state = BufferizedSendingState.SENT;
    }
}
