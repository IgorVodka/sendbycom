package iu5.sendbycom.application.message;

import iu5.sendbycom.application.CommonParams;
import iu5.sendbycom.application.datatypes.SessionHash;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameType;
import iu5.sendbycom.physical.exception.DataTooBigException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileContents extends SessionMessage {
    private String path;

    public FileContents(String path, SessionHash hash) {
        super(hash);
        this.path = path;
    }

    @Override
    public List<Frame> getFrames() throws DataTooBigException {
        List<Frame> frames = new ArrayList<Frame>();

        File file = new File(path);
        if (!file.isFile() || !file.canRead()) {
            frames.add(new Frame(FrameType.FILE_META, encodeSessionBytes(ByteUtils.longToBytes(-1))));
            return frames;
        }
        try {
            byte[] fileSizeBytes = ByteUtils.longToBytes(file.length());
            frames.add(new Frame(FrameType.FILE_META, encodeSessionBytes(fileSizeBytes)));

            FileInputStream is = new FileInputStream(file);
            byte[] buffer = new byte[CommonParams.PART_SIZE];
            int readBytes;
            while ((readBytes = is.read(buffer)) != -1) {
                //System.out.println("Read a part of file: " + readBytes + " bytes.");
                byte[] part = Arrays.copyOfRange(buffer, 0, readBytes);
                frames.add(new Frame(FrameType.FILE_PART, encodeSessionBytes(part)));
            }
        } catch (IOException e) {
            frames.add(new Frame(FrameType.FILE_META, encodeSessionBytes("failed to read")));
            return frames;
        }

        return frames;
    }
}
