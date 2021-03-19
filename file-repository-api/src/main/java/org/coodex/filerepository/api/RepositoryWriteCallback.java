package org.coodex.filerepository.api;

import java.io.OutputStream;

/**
 * file save callback
 */
public interface RepositoryWriteCallback {
    /**
     * write file to repository, only invoke once when write file
     * @param outputStream  output stream to save file content
     * @return  0: success, non-zero: fail.
     */
    int write(OutputStream outputStream);
}
