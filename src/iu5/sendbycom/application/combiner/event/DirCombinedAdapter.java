package iu5.sendbycom.application.combiner.event;

import java.util.EventListener;

public interface DirCombinedAdapter extends EventListener {
    void onDirCombined(DirCombinedEvent event);
}
