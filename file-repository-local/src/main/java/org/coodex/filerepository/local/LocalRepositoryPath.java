package org.coodex.filerepository.local;

/**
 * local repository base path
 */
public class LocalRepositoryPath {
    /**
     * base path
     */
    private String location;
    /**
     * read file from this path
     */
    private boolean readable;
    /**
     * write file to this path
     */
    private boolean writable;

    public static LocalRepositoryPath build(String location, boolean readable, boolean writable) {
        LocalRepositoryPath path = new LocalRepositoryPath();
        path.location = location;
        path.readable = readable;
        path.writable = writable;
        return path;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }
}
