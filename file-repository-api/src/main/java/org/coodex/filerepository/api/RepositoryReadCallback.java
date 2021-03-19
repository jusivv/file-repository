package org.coodex.filerepository.api;

/**
 * file read callback
 */
public interface RepositoryReadCallback {
    /**
     * read file in repository, maybe invoke more then once when read file
     * @param buff      data buffer
     * @param len       data length in buffer
     * @param fileSize  total file size
     * @return          0: success, non-zero: fail.
     */
    int read(byte[] buff, int len, long fileSize);
}
