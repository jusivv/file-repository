package org.coodex.file.repository.alioss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.TagSet;
import org.coodex.filerepository.api.*;
import org.coodex.util.UUIDHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

public class AliOssFileRepository implements IFileRepository {
    private static Logger log = LoggerFactory.getLogger(AliOssFileRepository.class);

    public static final int DIRECTORY_TYPE_NONE = 0;
    public static final int DIRECTORY_TYPE_YEAR = 1;
    public static final int DIRECTORY_TYPE_MONTH = 2;
    public static final int DIRECTORY_TYPE_DAY = 3;
    public static final String NAME_SPLITTER = "/";

    private String endpoint, accessKeyId, accessKeySecret, bucketName;

    private int directoryType = DIRECTORY_TYPE_NONE;

    private OSSClientBuilder ossClientBuilder;

    public AliOssFileRepository(String endpoint, String accessKeyId, String accessKeySecret, String bucketName,
                                int directoryType) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.directoryType = directoryType;
        ossClientBuilder = new OSSClientBuilder();
    }

    public AliOssFileRepository(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        this(endpoint, accessKeyId, accessKeySecret, bucketName, DIRECTORY_TYPE_NONE);
    }

    private String adjustDate(int date) {
        if (date < 10) {
            return "0" + date;
        } else {
            return String.valueOf(date);
        }
    }

    private String generateFileId() {
        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        if (directoryType > DIRECTORY_TYPE_NONE) {
            sb.append(calendar.get(Calendar.YEAR)).append(NAME_SPLITTER);
        }
        if (directoryType > DIRECTORY_TYPE_YEAR) {
            sb.append(adjustDate(calendar.get(Calendar.MONTH) + 1)).append(NAME_SPLITTER);
        }
        if (directoryType > DIRECTORY_TYPE_MONTH) {
            sb.append(adjustDate(calendar.get(Calendar.DAY_OF_MONTH))).append(NAME_SPLITTER);
        }
        sb.append(UUIDHelper.getUUIDString());
        return sb.toString();
    }

    private void saveFile(InputStream inputStream, FileMetaInf fileMetaInf, String fileId) {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            StoredFileMetaInf storedFileMetaInf = StoredFileMetaInf.from(fileMetaInf);
            storedFileMetaInf.setStoreTime(System.currentTimeMillis());
            objectMetadata.setObjectTagging(ObjectTaggingHelper.buildTags(storedFileMetaInf));
            ossClient.putObject(bucketName, fileId, inputStream, objectMetadata);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String save(InputStream inputStream, FileMetaInf fileMetaInf) throws Throwable {
        String fileId = generateFileId();
        saveFile(inputStream, fileMetaInf, fileId);
        return fileId;
    }

    @Override
    public String save(FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback) throws Throwable {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            writeCallback.write(outputStream);
            outputStream.flush();
            String fileId = generateFileId();
            saveFile(new ByteArrayInputStream(outputStream.toByteArray()), fileMetaInf, fileId);
            return fileId;
        } finally {
            outputStream.close();
        }
    }

    @Override
    public void get(String fileId, OutputStream outputStream) throws Throwable {
        get(fileId, ((buff, len, fileSize) -> {
            outputStream.write(buff, 0, len);
        }));
    }

    @Override
    public void get(String fileId, RepositoryReadCallback readCallback) throws Throwable {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fileId);
            if (ossObject == null) {
                throw new RuntimeException("OSS object not found.");
            }
            InputStream inputStream = ossObject.getObjectContent();
            try {
                byte[] buff = new byte[4 * 1024];
                int len = 0;
                long size = ossObject.getObjectMetadata().getContentLength();
                while ((len = inputStream.read(buff)) > 0) {
                    readCallback.read(buff, len, size);
                }
            } finally {
                ossObject.close();
            }
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void delete(String fileId) throws Throwable {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.deleteObject(bucketName, fileId);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public StoredFileMetaInf getMetaInf(String fileId) throws Throwable {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            TagSet tagSet = ossClient.getObjectTagging(bucketName, fileId);
            return ObjectTaggingHelper.parseFileMetaInf(tagSet);
        } finally {
          ossClient.shutdown();
        }
    }

    @Override
    public String asyncSave(InputStream inputStream, FileMetaInf fileMetaInf, RepositoryNotifyCallback notifyCallback) {
        final String fileId = generateFileId();
        new Thread(() -> {
            try {
                saveFile(inputStream, fileMetaInf, fileId);
                notifyCallback.complete(true, fileId, null);
            } catch (Throwable t) {
                log.error(t.getLocalizedMessage(), t);
                notifyCallback.complete(false, fileId, t);
            }

        }).start();
        return fileId;
    }

    @Override
    public String asyncSave(FileMetaInf fileMetaInf, RepositoryWriteCallback writeCallback, RepositoryNotifyCallback notifyCallback) {
        final String fileId = generateFileId();
        new Thread(() -> {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                writeCallback.write(outputStream);
                outputStream.close();
                saveFile(new ByteArrayInputStream(outputStream.toByteArray()), fileMetaInf, fileId);
                notifyCallback.complete(true, fileId, null);
            } catch (Throwable t) {
                log.error(t.getLocalizedMessage(), t);
                notifyCallback.complete(false, fileId, t);
            }
        }).start();
        return fileId;
    }

    @Override
    public String asyncDelete(String fileId, RepositoryNotifyCallback notifyCallback) {
        new Thread(() -> {
            try {
                delete(fileId);
                notifyCallback.complete(true, fileId, null);
            } catch (Throwable t) {
                log.error(t.getLocalizedMessage(), t);
                notifyCallback.complete(false, fileId, t);
            }
        }).start();
        return fileId;
    }
}
