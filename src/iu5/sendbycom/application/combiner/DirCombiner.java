package iu5.sendbycom.application.combiner;

import iu5.sendbycom.application.combiner.event.DirCombinedAdapter;
import iu5.sendbycom.application.combiner.event.DirCombinedEvent;
import iu5.sendbycom.application.datatypes.DirFile;
import iu5.sendbycom.application.datatypes.SessionData;
import iu5.sendbycom.application.datatypes.SessionHash;
import iu5.sendbycom.application.exception.MetaNotReceivedException;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameType;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class DirCombiner extends Combiner {
    private List<DirFile> fileList;
    private int filesToDownload;

    public DirCombiner(DirCombinedAdapter listener) {
        super(listener);
        this.fileList = new ArrayList<DirFile>();
        this.filesToDownload = -1;
    }

    @Override
    public void add(FrameType frameType, SessionData data) throws MetaNotReceivedException {
        byte[] body = data.getBody();
        DirCombinedAdapter adapter = (DirCombinedAdapter) getListener();

        if (frameType == FrameType.DIR_CONTENTS_META) {
            int filesCount = (int) ByteUtils.bytesToLong(body);

            if (filesCount == -1) {
                adapter.onDirCombined(new DirCombinedEvent(false, null));
            } else if (filesCount == 0) {
                // Send empty
                adapter.onDirCombined(new DirCombinedEvent(true, fileList));
            } else {
                filesToDownload = filesCount;
            }
        } else if (frameType == FrameType.DIR_CONTENTS_PART) {
            String typeAndName = new String(body);
            DirFile dirFile = new DirFile(typeAndName.charAt(0) == 'F', typeAndName.substring(1));
            fileList.add(dirFile);

            if (filesToDownload == -1) {
                // First should receive meta
                throw new MetaNotReceivedException();
            }

            if (this.fileList.size() == filesToDownload) {
                adapter.onDirCombined(new DirCombinedEvent(true, fileList));
            }
        }
    }
}
