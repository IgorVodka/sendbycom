package iu5.sendbycom.application.combiner;

import iu5.sendbycom.application.datatypes.SessionData;
import iu5.sendbycom.application.datatypes.SessionHash;
import iu5.sendbycom.application.exception.MetaNotReceivedException;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameType;

import java.io.IOException;
import java.util.EventListener;

public abstract class Combiner {
    private EventListener listener;

    public Combiner(EventListener listener) {
        this.listener = listener;
    }

    public EventListener getListener() {
        return listener;
    }

    public abstract void add(FrameType frameType, SessionData data) throws MetaNotReceivedException, IOException;
}
