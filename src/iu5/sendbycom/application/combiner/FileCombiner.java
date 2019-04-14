package iu5.sendbycom.application.combiner;

import iu5.sendbycom.application.CommonParams;
import iu5.sendbycom.application.combiner.event.*;
import iu5.sendbycom.application.datatypes.DirFile;
import iu5.sendbycom.application.datatypes.SessionData;
import iu5.sendbycom.application.exception.MetaNotReceivedException;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.FrameType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileCombiner extends Combiner {
    private List<byte[]> parts;
    private int partsToDownload;

    public FileCombiner(FileCombinedAdapter listener) {
        super(listener);
        this.parts = new ArrayList<byte[]>();
        this.partsToDownload = -1;
    }

    @Override
    public void add(FrameType frameType, SessionData data) throws MetaNotReceivedException, IOException {
        byte[] body = data.getBody();
        FileCombinedAdapter adapter = (FileCombinedAdapter) getListener();

        if (frameType == FrameType.FILE_META) {
            // TODO: SOLVE THIS. BAD LONG IS RECEIVED FOR PDF FILE
            int partsCount = (int) Math.ceil((double) ByteUtils.bytesToLong(body) / CommonParams.PART_SIZE);

            if (partsCount == -1) {
                adapter.onFileCombined(new FileCombinedEvent(false, null));
            } else if (partsCount == 0) {
                // Send empty
                adapter.onFileCombined(new FileCombinedEvent(true, parts));
            } else {
                partsToDownload = partsCount;
            }

            adapter.onFileStarted(partsCount);
        } else if (frameType == FrameType.FILE_PART) {
            parts.add(body);

            if (partsToDownload == -1) {
                // First should receive meta
                throw new MetaNotReceivedException();
            }

            adapter.onFilePartReceived(new FilePartReceivedEvent(0, body)); // TODO: number = 0

            System.out.println(parts.size() + "/" + partsToDownload); // todo

            if (parts.size() == partsToDownload) {
                adapter.onFileCombined(new FileCombinedEvent(true, parts));
            }
        }
    }
}
