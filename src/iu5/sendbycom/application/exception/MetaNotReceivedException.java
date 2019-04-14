package iu5.sendbycom.application.exception;

public class MetaNotReceivedException extends Exception {
    @Override
    public String getMessage() {
        return "Not received meta for file!";
    }
}
