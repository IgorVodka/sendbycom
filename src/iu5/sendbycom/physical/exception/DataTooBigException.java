package iu5.sendbycom.physical.exception;

import iu5.sendbycom.link.Frame;

public class DataTooBigException extends Exception {
    @Override
    public String getMessage() {
        return String.format("Data should be no more than %d bytes!", Frame.MAX_DATA_SIZE);
    }
}
