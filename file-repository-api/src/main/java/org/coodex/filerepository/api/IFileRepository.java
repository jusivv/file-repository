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
     * @throws Throwable
     */
    String save(InputStream inputStream, FileMetaInf fileMetaInf) throws Throwable;

    /**
     * save file in async model
     * @param inputStream       input
     * @param fileMetaInf       file meta information
     * @param notifyCallback    notify callback
     * @return                  file id
     */
    String asyncSave(InputStream inputStream, FileMetaInf fileMetaInf, RepositoryNotifyCallback notifyCallback);

    /**
     * get file
     * @param fileId        file id
     * @param outputStream  output stream to write file content
     * @throws Throwable
     */
    void get(String fileId, OutputStream outputStream) throws Throwable;

    /**
     * get file block
     * @param fileId        file id
     * @param offset        read offset
     * @param length        read length, 0: read to the end
     * @param outputStream  output stream to write file block
     * @throws Throwable
     */
    void get(String fileId, long offset, int length, OutputStream outputStream) throws Throwable;

    /**
     * delete file in repository
     * @param fileId        file id
     * @throws Throwable
     */
    void delete(String fileId) throws Throwable;

    /**
     * delete file in async model
     * @param fileId            file id
     * @param notifyCallback    notify callback
     */
    void asyncDelete(String fileId, RepositoryNotifyCallback notifyCallback);

    /**
     * get file meta-inf stored in repository
     * @param fileId        file id
     * @return              file meta-info
     * @throws Throwable
     */
    FileMetaInf getMetaInf(String fileId) throws Throwable;
}
