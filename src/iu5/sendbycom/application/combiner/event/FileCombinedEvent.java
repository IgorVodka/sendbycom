package iu5.sendbycom.application.combiner.event;

import iu5.sendbycom.application.datatypes.DirFile;

import java.util.List;

public class FileCombinedEvent {
    private boolean success;
    private List<byte[]> parts;

    public FileCombinedEvent(boolean success, List<byte[]> parts) {
        this.success = success;
        this.parts = parts;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<byte[]> getParts() {
        return parts;
    }
}
