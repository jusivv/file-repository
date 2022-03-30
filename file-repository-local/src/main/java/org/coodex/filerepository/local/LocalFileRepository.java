package org.coodex.filerepository.local;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.*;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class LocalFileRepository implements IFileRepository {
    private static Logger log = LoggerFactory.getLogger(LocalFileRepository.class);
    private static Profile profile = Profile.get("localFileRepository.properties");

    private LocalRepositoryPath[] basePaths;
    private IPathGenerator[] pathGenerators;

    public LocalFileRepository(LocalRepositoryPath[] basePaths, IPathGenerator ... pathGenerators) {
        this.pathGenerators = pathGenerators;
        Map<String, LocalRepositoryPath> pathMap = new HashMap<>();
        boolean hasReadDir = false, hasWriteDir = false;
        for (LocalRepositoryPath path : basePaths) {
            if (!path.isCanRead() && !path.isCanWrite()) {
                continue;
            }
            String basePath = path.getBasePath().endsWith(File.separator) ? path.getBasePath() : path.getBasePath()
                    + File.separator;
            if (pathMap.containsKey(basePath)) {
                continue;
            }
            File fp = new File(path.getBasePath());
            if (!fp.isDirectory()) {
                throw new RuntimeException("Invalid base path: " + path.getBasePath());
            }
            fp = new File(basePath + "." + UuidHelper.getUUIDString());
            if (path.isCanWrite() && !fp.mkdirs()) {
                throw new RuntimeException("Fail to write directory: " + path.getBasePath());
            } else {
                fp.delete();
            }
            LocalRepositoryPath localRepositoryPath = new LocalRepositoryPath(basePath, path.isCanRead(),
                    path.isCanWrite());
            if (localRepositoryPath.isCanRead()) {
                hasReadDir = true;
            }
            if (localRepositoryPath.isCanWrite()) {
                hasWriteDir = true;
            }
            pathMap.put(basePath, localRepositoryPath);
        }
        if (!hasReadDir) {
            throw new RuntimeException("There is no directory to read");
        }
        if (!hasWriteDir) {
            throw new RuntimeException("There is no directory to write");
        }
        this.basePaths = pathMap.values().toArray(new LocalRepositoryPath[0]);
    }

    public LocalFileRepository(String basePath, IPathGenerator ... pathGenerators) {
        this(new LocalRepositoryPath[]{new LocalRepositoryPath(basePath, true, true)}, pathGenerators);
    }

    private String getPath(String seed, String basePath) {
        StringBuilder path = new StringBuilder(basePath);
        for (IPathGenerator pathGenerator : pathGenerators) {
            String subPath = pathGenerator.getPath(seed);
            path.append(subPath.endsWith(File.separator) ? subPath : subPath + File.separatorChar);
        }
        return path.toString();
    }

    @Override
    public String save(InputStream inputStream, FileMetaInf fileMetaInf) throws Throwable {
        return save(fileMetaInf, outputStream -> Common.copyStream(inputStream, outputStream));
    }

    @Override
    public String save(FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback) throws Throwable {
        String fileId = UuidHelper.getUUIDString();
        saveFile(fileId, fileMetaInf, writeCallback);
        return fileId;
    }

    private void saveFile(String fileId, FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback)
            throws Throwable {
        FileOutputStreamWithMessageDigest digestOutputStream = null;
        MultipleOutputStream dataOutputStream = new MultipleOutputStream();
        MultipleOutputStream metaOutputStream = new MultipleOutputStream();
        String digestAlgorithm = profile.getString("digestAlgorithm", "MD5");
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isCanWrite()) {
                String filePath = getPath(fileId, path.getBasePath());
                log.debug("save file {} to {}", fileId, filePath);
                File parentPath = new File(filePath);
                if (!parentPath.exists()) {
                    parentPath.mkdirs();
                }
                String dataFilePath = filePath + fileId + ".data";
                if (digestOutputStream == null) {
                    digestOutputStream = new FileOutputStreamWithMessageDigest(new File(dataFilePath),
                            MessageDigest.getInstance(digestAlgorithm));
                    dataOutputStream.addOutputStream(dataFilePath, new BufferedOutputStream(digestOutputStream));
                } else {
                    dataOutputStream.addOutputStream(dataFilePath, new BufferedOutputStream(new FileOutputStream(dataFilePath)));
                }
                String metaFilePath = filePath + fileId + ".json";
                metaOutputStream.addOutputStream(metaFilePath, new BufferedOutputStream(new FileOutputStream(metaFilePath)));
            }
        }
        if (digestOutputStream != null) {
            try {
                writeCallback.write(dataOutputStream);
            } finally {
                dataOutputStream.close();
            }
            StoredFileMetaInf storedFileMetaInf = StoredFileMetaInf.from(fileMetaInf);
            storedFileMetaInf.setHashAlgorithm(digestAlgorithm);
            storedFileMetaInf.setHashValue(digestOutputStream.getDigestValue());
            try {
                metaOutputStream.write(JSON.toJSONString(storedFileMetaInf).getBytes(StandardCharsets.UTF_8));
            } finally {
                metaOutputStream.close();
            }
        }
    }

    @Override
    public void get(String fileId, OutputStream outputStream) throws Throwable {
        get(fileId, (buff, len, fileSize) -> outputStream.write(buff, 0, len));
    }

    @Override
    public void get(String fileId, RepositoryReadCallback readCallback) throws Throwable {
        boolean read = false;
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isCanRead()) {
                String filePath = getPath(fileId, path.getBasePath());
                File dataFile = new File(filePath + fileId + ".data");
                if (dataFile.exists()) {
                    byte[] buff = new byte[4 * 1024];
                    int len = 0;
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(dataFile));
                    try {
                        long fileSize = dataFile.length();
                        while ((len = inputStream.read(buff)) > 0) {
                            readCallback.read(buff, len, fileSize);
                        }
                        log.debug("read file {} from {}", fileId, path.getBasePath());
                        read = true;
                        break;
                    } finally {
                        inputStream.close();
                    }
                }
            }
        }
        if (!read) {
            throw new RuntimeException("file not found: " + fileId);
        }
    }

    @Override
    public void delete(String fileId) throws Throwable {
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isCanWrite()) {
                String filePath = getPath(fileId, path.getBasePath());
                deleteFile(filePath, fileId);
                log.debug("delete file {} from {}", fileId, path.getBasePath());
            }
        }
    }

    private void deleteFile(String filePath, String fileId) {
        deleteLocalFile(filePath + fileId + ".data");
        deleteLocalFile(filePath + fileId + ".json");
    }


    private void deleteLocalFile(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    public StoredFileMetaInf getMetaInf(String fileId) throws Throwable {
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isCanRead()) {
                String filePath = getPath(fileId, path.getBasePath());
                File metaFile = new File(filePath + fileId + ".json");
                if (metaFile.exists()) {
                    InputStream inputStream = new FileInputStream(metaFile);
                    try {
                        return JSON.parseObject(inputStream, StoredFileMetaInf.class);
                    } finally {
                        inputStream.close();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String asyncSave(InputStream inputStream, FileMetaInf fileMetaInf, RepositoryNotifyCallback notifyCallback) {
        return asyncSave(fileMetaInf, outputStream -> Common.copyStream(inputStream, outputStream), notifyCallback);
    }

    @Override
    public String asyncSave(FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback,
                            RepositoryNotifyCallback notifyCallback) {
        final String fileId = UuidHelper.getUUIDString();
        new Thread(() -> {
            try {
                saveFile(fileId, fileMetaInf, writeCallback);
                notifyCallback.complete(true, fileId, null);
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage(), e);
                notifyCallback.complete(false, fileId, e);
            }
        }).start();
        return fileId;
    }

    @Override
    public String asyncDelete(String fileId, RepositoryNotifyCallback notifyCallback) {
        new Thread(() -> {
            for (LocalRepositoryPath path : this.basePaths) {
                if (path.isCanWrite()) {
                    String filePath = getPath(fileId, path.getBasePath());
                    deleteFile(filePath, fileId);
                    log.debug("delete file {} from {}", fileId, path.getBasePath());
                }
            }
        }).start();
        return fileId;
    }
}
