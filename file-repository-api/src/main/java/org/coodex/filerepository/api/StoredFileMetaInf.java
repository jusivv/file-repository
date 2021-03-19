package org.coodex.filerepository.api;

/**
 * file meta-inf in repository
 */
public class StoredFileMetaInf extends FileMetaInf {
    /**
     * file hash value with hex
     */
    private String hashValue;
    /**
     * hash algorithm
     */
    private String hashAlgorithm;
    /**
     * timestamp when file stored
     */
    private long storeTime;

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public long getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }

    public static StoredFileMetaInf from(FileMetaInf fileMetaInf) {
        StoredFileMetaInf storedFileMetaInf = new StoredFileMetaInf();
        if (fileMetaInf != null) {
            storedFileMetaInf.setFileName(fileMetaInf.getFileName());
            storedFileMetaInf.setExtName(fileMetaInf.getExtName());
            storedFileMetaInf.setFileSize(fileMetaInf.getFileSize());
            storedFileMetaInf.setClientId(fileMetaInf.getClientId());
        }
        storedFileMetaInf.setStoreTime(System.currentTimeMillis());
        return storedFileMetaInf;
    }
}
