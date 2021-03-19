package org.coodex.filerepository.api;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * file repository
 */
public interface IFileRepository {
    /**
     * save file
     * @param inputStream   input stream to read file content
     * @param fileMetaInf   file meta-inf
     * @return  file id
     */
    String save(InputStream inputStream, FileMetaInf fileMetaInf);

    /**
     * save file with callback function
     * @param fileMetaInf   file meta-inf
     * @param callback      write callback
     * @return              file id
     */
    String save(FileMetaInf fileMetaInf, RepositoryWriteCallback callback);

    /**
     * get file
     * @param fileId        file id
     * @param outputStream  output stream to write file content
     */
    void get(String fileId, OutputStream outputStream);

    /**
     * get file with callback function
     * @param fileId        file id
     * @param callback      read callback
     */
    void get(String fileId, RepositoryReadCallback callback);

    /**
     * delete file in repository
     * @param fileId        file id
     * @return              0: success, 1: file not found, other: error code
     */
    int delete(String fileId);

    /**
     * get file meta-inf stored in repository
     * @param fileId        file id
     * @return              file meta-info
     */
    StoredFileMetaInf getMetaInf(String fileId);
}
