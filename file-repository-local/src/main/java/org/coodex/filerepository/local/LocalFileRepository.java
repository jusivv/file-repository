package org.coodex.filerepository.local;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.AbstractFileRepository;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LocalFileRepository extends AbstractFileRepository {
    private static Logger log = LoggerFactory.getLogger(LocalFileRepository.class);
//    private static Profile profile = Profile.get("localFileRepository.properties");

    private LocalRepositoryPath[] basePaths;
    private IPathGenerator[] pathGenerators;

    public LocalFileRepository(LocalRepositoryPath[] basePaths, IPathGenerator ... pathGenerators) {
        this.pathGenerators = pathGenerators;
        Map<String, LocalRepositoryPath> pathMap = new HashMap<>();
        boolean hasReadDir = false, hasWriteDir = false;
        for (LocalRepositoryPath path : basePaths) {
            if (!path.isReadable() && !path.isWritable()) {
                continue;
            }
            String basePath = path.getLocation().endsWith(File.separator) ? path.getLocation() : path.getLocation()
                    + File.separator;
            if (pathMap.containsKey(basePath)) {
                continue;
            }
            File fp = new File(path.getLocation());
            if (!fp.isDirectory()) {
                throw new RuntimeException("Invalid base path: " + path.getLocation());
            }
            fp = new File(basePath + "." + UuidHelper.getUUIDString());
            if (path.isWritable() && !fp.mkdirs()) {
                throw new RuntimeException("Fail to write directory: " + path.getLocation());
            } else {
                fp.delete();
            }
            LocalRepositoryPath localRepositoryPath = LocalRepositoryPath.build(basePath, path.isReadable(),
                    path.isWritable());
            if (localRepositoryPath.isReadable()) {
                hasReadDir = true;
            }
            if (localRepositoryPath.isWritable()) {
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
        this(new LocalRepositoryPath[]{LocalRepositoryPath.build(basePath, true, true)}, pathGenerators);
    }

    private String getPath(String seed, String basePath) {
        StringBuilder path = new StringBuilder(basePath);
        if (!basePath.endsWith(File.separator)) {
            path.append(File.separator);
        }
        for (IPathGenerator pathGenerator : pathGenerators) {
            String subPath = pathGenerator.getPath(seed);
            if (Common.isBlank(subPath)) {
                continue;
            }
            if (subPath.startsWith(File.separator)) {
                subPath = subPath.substring(1);
            }
            path.append(subPath.endsWith(File.separator) ? subPath : subPath + File.separator);
        }
        return path.toString();
    }

    @Override
    protected String generateFileId(String clientId) {
        return (Common.isBlank(clientId) ? "" : clientId + "$") + UuidHelper.getUUIDString();
    }

    @Override
    protected <T extends FileMetaInf> void saveFile(String fileId, InputStream inputStream, T fileMetaInf)
            throws Throwable {
        MultipleOutputStream dataOutputStream = new MultipleOutputStream();
        MultipleOutputStream metaOutputStream = new MultipleOutputStream();
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isWritable()) {
                String filePath = getPath(fileId, path.getLocation());
                log.debug("save file {} to {}", fileId, path.getLocation());
                File parentPath = new File(filePath);
                if (!parentPath.exists()) {
                    parentPath.mkdirs();
                }
                String dataFilePath = filePath + fileId + ".data";
                dataOutputStream.addOutputStream(dataFilePath, new BufferedOutputStream(new FileOutputStream(dataFilePath)));
                String metaFilePath = filePath + fileId + ".json";
                metaOutputStream.addOutputStream(metaFilePath, new BufferedOutputStream(new FileOutputStream(metaFilePath)));
            }
        }
        try {
            Common.copyStream(inputStream, dataOutputStream);
        } finally {
            dataOutputStream.close();
        }
        try {
            metaOutputStream.write(JSON.toJSONString(fileMetaInf).getBytes(StandardCharsets.UTF_8));
        } finally {
            metaOutputStream.close();
        }
    }

    @Override
    public void get(String fileId, long offset, int length, OutputStream outputStream) throws Throwable {
        boolean read = false;
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isReadable()) {
                String filePath = getPath(fileId, path.getLocation());
                File dataFile = new File(filePath + fileId + ".data");
                if (dataFile.exists()) {
                    byte[] buff = new byte[4 * 1024];
                    int len = 0;
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(dataFile));
                    try {
                        long fileSize = dataFile.length();
                        if (length > 0) {
                            fileSize = Math.min(fileSize, length);

                        }
                        if (offset > 0) {
                            inputStream.skip(offset);
                        }
                        long restSize = fileSize;
                        while ((len = inputStream.read(buff)) > 0) {
                            if (restSize > len) {
                                outputStream.write(buff, 0, len);
                            } else {
                                outputStream.write(buff, 0, (int) restSize);
                                break;
                            }
                            restSize -= len;
                        }
                        outputStream.flush();
                        log.debug("read file {} from {}", fileId, path.getLocation());
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
            if (path.isWritable()) {
                String filePath = getPath(fileId, path.getLocation());
                deleteFile(filePath, fileId);
                log.debug("delete file {} from {}", fileId, path.getLocation());
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
    public <T extends FileMetaInf> T getMetaInf(String fileId, Class<T> clazz) throws Throwable {
        for (LocalRepositoryPath path : this.basePaths) {
            if (path.isReadable()) {
                String filePath = getPath(fileId, path.getLocation());
                File metaFile = new File(filePath + fileId + ".json");
                if (metaFile.exists()) {
                    InputStream inputStream = new FileInputStream(metaFile);
                    try {
                        return JSON.parseObject(inputStream, clazz);
                    } finally {
                        inputStream.close();
                    }
                }
            }
        }
        return null;
    }
}
