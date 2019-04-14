package iu5.sendbycom.physical;

public class UnknownPortParameter extends Exception {
    private String key;
    private Object value;

    public UnknownPortParameter(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getMessage() {
        return String.format("Unknown port parameter %s = %s!", key, value);
    }
}
