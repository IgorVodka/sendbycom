package iu5.sendbycom.application.combiner.event;

import iu5.sendbycom.application.datatypes.DirFile;

import java.util.EventListener;
import java.util.List;

public class DirCombinedEvent {
    private boolean success;
    private List<DirFile> files;

    public DirCombinedEvent(boolean success, List<DirFile> files) {
        this.success = success;
        this.files = files;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<DirFile> getFiles() {
        return files;
    }
}
