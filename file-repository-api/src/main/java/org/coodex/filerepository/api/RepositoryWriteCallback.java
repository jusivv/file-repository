package org.coodex.filerepository.api;

import java.io.OutputStream;

/**
 * file save callback
 */
@FunctionalInterface
public interface RepositoryWriteCallback {
    /**
     * write file to repository, only invoke once when write file
     * @param outputStream  output stream to save file content
     * @return  0: success, non-zero: fail.
     */
    void write(OutputStream outputStream) throws Throwable;
}
