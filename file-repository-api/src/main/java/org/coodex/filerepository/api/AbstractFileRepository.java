package org.coodex.filerepository.api;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractFileRepository implements IFileRepository {

    protected abstract String generateFileId(String clientId);

    protected abstract void saveFile(String fileId, InputStream inputStream, FileMetaInf fileMetaInf) throws Throwable;
    @Override
    public String save(InputStream inputStream, FileMetaInf fileMetaInf) throws Throwable {
        String fileId = generateFileId(fileMetaInf.getClientId());
        saveFile(fileId, inputStream, fileMetaInf);
        return fileId;
    }

    @Override
    public String asyncSave(InputStream inputStream, FileMetaInf fileMetaInf, RepositoryNotifyCallback notifyCallback) {
        String fileId = generateFileId(fileMetaInf.getClientId());
        new Thread(() -> {
            try {
                saveFile(fileId, inputStream, fileMetaInf);
                notifyCallback.complete(true, fileId, null);
            } catch (Throwable e) {
                notifyCallback.complete(false, fileId, e);
            }
        }).start();
        return fileId;
    }

    @Override
    public void get(String fileId, OutputStream outputStream) throws Throwable {
        get(fileId, 0, 0, outputStream);
    }

    @Override
    public abstract void get(String fileId, long offset, int length, OutputStream outputStream) throws Throwable;

    @Override
    public abstract void delete(String fileId) throws Throwable;

    @Override
    public void asyncDelete(String fileId, RepositoryNotifyCallback notifyCallback) {
        new Thread(() -> {
            try {
                delete(fileId);
                notifyCallback.complete(true, fileId, null);
            } catch (Throwable e) {
                notifyCallback.complete(false, fileId, e);
            }
        }).start();
    }

    @Override
    public abstract FileMetaInf getMetaInf(String fileId) throws Throwable;
}
