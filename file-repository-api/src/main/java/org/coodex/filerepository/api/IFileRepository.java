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
     * save file with callback function
     * @param fileMetaInf   file meta-inf
     * @param writeCallback      write callback
     * @return              file id
     * @throws Throwable
     */
    String save(FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback) throws Throwable;

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
     * @param length        read length
     * @param outputStream  output stream to write file block
     * @throws Throwable
     */
    void get(String fileId, long offset, int length, OutputStream outputStream) throws Throwable;

    /**
     * get file with callback function
     * @param fileId        file id
     * @param readCallback  read callback
     * @throws Throwable
     */
    void get(String fileId, RepositoryReadCallback readCallback) throws Throwable;

    /**
     * get file block with callback function
     * @param fileId        file id
     * @param offset        read offset
     * @param length        read length
     * @param readCallback  read callback
     * @throws Throwable
     */
    void get(String fileId, long offset, int length, RepositoryReadCallback readCallback) throws Throwable;

    /**
     * delete file in repository
     * @param fileId        file id
     * @throws Throwable
     */
    void delete(String fileId) throws Throwable;

    /**
     * get file meta-inf stored in repository
     * @param fileId        file id
     * @return              file meta-info
     * @throws Throwable
     */
    StoredFileMetaInf getMetaInf(String fileId) throws Throwable;

    /**
     * save file in async model
     * @param inputStream       input
     * @param fileMetaInf       file meta information
     * @param notifyCallback    notify callback
     * @return                  file id
     */
    String asyncSave(InputStream inputStream, FileMetaInf fileMetaInf, RepositoryNotifyCallback notifyCallback);

    /**
     * save file in async model with write callback
     * @param fileMetaInf       file meta information
     * @param writeCallback     write callback
     * @param notifyCallback    notify callback
     * @return                  file id
     */
    String asyncSave(FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback,
                     RepositoryNotifyCallback notifyCallback);

    /**
     * delete file in async model
     * @param fileId            file id
     * @param notifyCallback    notify callback
     * @return                  file id
     */
    String asyncDelete(String fileId, RepositoryNotifyCallback notifyCallback);

}
