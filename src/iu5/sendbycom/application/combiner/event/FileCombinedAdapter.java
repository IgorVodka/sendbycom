package iu5.sendbycom.application.combiner.event;

import java.io.IOException;
import java.util.EventListener;

public interface FileCombinedAdapter extends EventListener {
    void onFileStarted(int totalParts);
    void onFileCombined(FileCombinedEvent event) throws IOException;
    void onFilePartReceived(FilePartReceivedEvent event) throws IOException;
}
