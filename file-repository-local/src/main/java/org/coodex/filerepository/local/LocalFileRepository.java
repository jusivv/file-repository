package org.coodex.filerepository.local;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.*;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LocalFileRepository implements IFileRepository {
    private static Logger log = LoggerFactory.getLogger(LocalFileRepository.class);
    private static Profile profile = Profile.get("localFileRepository.properties");

    private String basePath;
    private IPathGenerator[] pathGenerators;

    public LocalFileRepository(String basePath, IPathGenerator ... pathGenerators) {
        this.basePath = basePath.endsWith(File.separator) ? basePath : basePath + File.separatorChar;
        this.pathGenerators = pathGenerators;
    }

    private String getPath(String seed) {
        StringBuilder path = new StringBuilder(basePath);
        for (IPathGenerator pathGenerator : pathGenerators) {
            String subPath = pathGenerator.getPath(seed);
            path.append(subPath.endsWith(File.separator) ? subPath : subPath + File.separatorChar);
        }
        return path.toString();
    }

    @Override
    public String save(InputStream inputStream, FileMetaInf fileMetaInf) {
        return save(fileMetaInf, new RepositoryWriteCallback() {
            @Override
            public int write(OutputStream outputStream) {
                try {
                    Common.copyStream(inputStream, outputStream);
                    return 0;
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    return 1;
                }
            }
        });
    }

    @Override
    public String save(FileMetaInf fileMetaInf, RepositoryWriteCallback callback) {
        String fileId = Common.getUUIDStr();
        String filePath = getPath(fileId);
        File path = new File(filePath);
        if (!path.exists()) {
            path.mkdirs();
        }
        File dataFile = new File(filePath + fileId + ".data");
        File metaFile = new File(filePath + fileId + ".json");
        try {
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
            // use buffer
            String digestAlgorithm = profile.getString("digestAlgorithm", "MD5");
            FileOutputStreamWithMessageDigest digestOutputStream = new FileOutputStreamWithMessageDigest(dataFile,
                    MessageDigest.getInstance(digestAlgorithm));
            OutputStream outputStream = new BufferedOutputStream(digestOutputStream);
            try {
                int code = callback.write(outputStream);
                if (code != 0) {
                    throw new RuntimeException("fail to write file to repository with error code: " + code);
                }
            } finally {
                outputStream.close();
            }

            if (!metaFile.exists()) {
                metaFile.createNewFile();
            }
            StoredFileMetaInf storedFileMetaInf = StoredFileMetaInf.from(fileMetaInf);
            storedFileMetaInf.setHashAlgorithm(digestAlgorithm);
            storedFileMetaInf.setHashValue(digestOutputStream.getDigestValue());
            FileWriter fileWriter = new FileWriter(metaFile, Charset.forName("UTF-8"));
            try {
                fileWriter.write(JSON.toJSONString(storedFileMetaInf));
                fileWriter.flush();
            } finally {
                fileWriter.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
        return fileId;
    }

    @Override
    public void get(String fileId, OutputStream outputStream) {
        get(fileId, new RepositoryReadCallback() {
            @Override
            public int read(byte[] buff, int len, long fileSize) {
                try {
                    outputStream.write(buff, 0, len);
                    return 0;
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    return 1;
                }
            }
        });
    }

    @Override
    public void get(String fileId, RepositoryReadCallback callback) {
        String filePath = getPath(fileId);
        try {
            byte[] buff = new byte[4 * 1024];
            int len = 0;
            File dataFile = new File(filePath + fileId + ".data");
            InputStream inputStream = new BufferedInputStream(new FileInputStream(dataFile));
            try {
                long fileSize = dataFile.length();
                while ((len = inputStream.read(buff)) > 0) {
                    int code = callback.read(buff, len, fileSize);
                    if (code != 0) {
                        throw new RuntimeException("fail to read file from repository with error code: " + code);
                    }
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int delete(String fileId) {
        int re = 0;
        String filePath = getPath(fileId);
        re = deleteLocalFile(filePath + fileId + ".data");
        if (re == 0) {
            deleteLocalFile(filePath + fileId + ".json");
        }
        return re;
    }

    private int deleteLocalFile(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            try {
                if (f.delete()) {
                    return 0;
                } else {
                    return 9;
                }
            } catch (SecurityException e) {
                log.error(e.getLocalizedMessage(), e);
                return 99;
            }
        } else {
            return 1;
        }
    }

    @Override
    public StoredFileMetaInf getMetaInf(String fileId) {
        String filePath = getPath(fileId);
        File metaFile = new File(filePath + fileId + ".json");
        if (metaFile.exists()) {
            try {
                InputStream inputStream = new FileInputStream(metaFile);
                try {
                    return JSON.parseObject(inputStream, StoredFileMetaInf.class);
                } finally {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
