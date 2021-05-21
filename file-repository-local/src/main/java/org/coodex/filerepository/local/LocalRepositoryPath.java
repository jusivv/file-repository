package org.coodex.filerepository.local;

/**
 * local repository base path
 */
public class LocalRepositoryPath {
    /**
     * base path
     */
    private String basePath;
    /**
     * read file from this path
     */
    private boolean canRead;
    /**
     * write file to this path
     */
    private boolean canWrite;

    public LocalRepositoryPath(String basePath, boolean canRead, boolean canWrite) {
        this.basePath = basePath;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }
}
