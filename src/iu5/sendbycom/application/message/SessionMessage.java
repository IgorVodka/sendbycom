package iu5.sendbycom.application.message;

import iu5.sendbycom.application.datatypes.SessionData;
import iu5.sendbycom.application.datatypes.SessionHash;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.physical.exception.DataTooBigException;

import java.util.List;

abstract public class SessionMessage {
    private SessionHash hash;

    public SessionMessage(SessionHash hash) {
        this.hash = hash;
    }

    protected byte[] encodeSessionBytes(byte[] data) {
        return new SessionData(hash, data).encode();
    }

    protected byte[] encodeSessionBytes(String data) {
        return new SessionData(hash, data).encode();
    }

    abstract public List<Frame> getFrames() throws DataTooBigException;
}
