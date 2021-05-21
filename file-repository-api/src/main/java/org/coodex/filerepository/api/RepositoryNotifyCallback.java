package org.coodex.filerepository.api;

/**
 * notify callback in async model
 */
@FunctionalInterface
public interface RepositoryNotifyCallback {
    /**
     * complete notify in async model
     * @param success   success or failure
     * @param fileId    file id
     * @param t         throwable object if failing
     */
    void complete(boolean success, String fileId, Throwable t);
}
