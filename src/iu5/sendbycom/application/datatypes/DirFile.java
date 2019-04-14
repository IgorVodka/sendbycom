package iu5.sendbycom.application.datatypes;

public class DirFile {
    private boolean isFile;
    private String name;

    public DirFile(boolean isFile, String name) {
        this.isFile = isFile;
        this.name = name;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isDirectory() {
        return !isFile;
    }

    public String getName() {
        return name;
    }
}
