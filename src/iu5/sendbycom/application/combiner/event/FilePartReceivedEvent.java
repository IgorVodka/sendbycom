package iu5.sendbycom.application.combiner.event;

import java.util.List;

public class FilePartReceivedEvent {
    private int number;
    private byte[] part;

    public FilePartReceivedEvent(int number, byte[] part) {
        this.number = number;
        this.part = part;
    }

    public int getNumber() {
        return number;
    }

    public byte[] getPart() {
        return part;
    }
}
