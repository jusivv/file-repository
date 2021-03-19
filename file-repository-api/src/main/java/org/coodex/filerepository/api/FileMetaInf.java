package org.coodex.filerepository.api;

/**
 * file meta-info
 */
public class FileMetaInf {
    /**
     * file name, do not include extension name
     */
    private String fileName;
    /**
     * file extension name, do not include dot
     */
    private String extName;
    /**
     * file size in byte, required
     */
    private long fileSize;
    /**
     * client id who commit file
     */
    private String clientId;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
