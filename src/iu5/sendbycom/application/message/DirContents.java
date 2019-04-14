package iu5.sendbycom.application.message;

import iu5.sendbycom.application.datatypes.SessionHash;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameType;
import iu5.sendbycom.physical.exception.DataTooBigException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// todo extends DataType
public class DirContents extends SessionMessage {
    private String path;

    public DirContents(String path, SessionHash hash) {
        super(hash);
        this.path = path;
    }

    public List<Frame> getFrames() throws DataTooBigException {
        List<Frame> frames = new ArrayList<Frame>();

        File dir = new File(path);
        if (!dir.isDirectory()) {
            frames.add(new Frame(FrameType.DIR_CONTENTS_META, encodeSessionBytes(ByteUtils.longToBytes(-1))));
            return frames;
        }

        File[] files = dir.listFiles();

        if (files != null) {
            byte[] filesCount = ByteUtils.longToBytes(files.length);
            frames.add(new Frame(FrameType.DIR_CONTENTS_META, encodeSessionBytes(filesCount)));

            for (File file : files) {
                String name = (file.isDirectory() ? "D" : "F") + file.getName();
                frames.add(new Frame(FrameType.DIR_CONTENTS_PART, encodeSessionBytes(name)));
            }
        } else {
            frames.add(new Frame(FrameType.DIR_CONTENTS_META, encodeSessionBytes(ByteUtils.longToBytes(-1))));
        }

        return frames;
    }
}
