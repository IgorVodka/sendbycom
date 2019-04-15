package iu5.sendbycom.physical.exception;

public class PortClosedException extends Exception {
    private String portName;

    public PortClosedException(String portName) {
        this.portName = portName;
    }

    @Override
    public String getMessage() {
        return String.format("Port %s is closed! Use open() before using it.", portName);
    }
}
