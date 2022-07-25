package org.coodex.file.repository.alioss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.TagSet;
import org.coodex.filerepository.api.AbstractFileRepository;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.util.UUIDHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

public class AliOssFileRepository extends AbstractFileRepository {
    private static Logger log = LoggerFactory.getLogger(AliOssFileRepository.class);

    /**
     * directory for file: none
     */
    public static final int DIRECTORY_TYPE_NONE = 0;
    /**
     * directory for file: divide by year
     */
    public static final int DIRECTORY_TYPE_YEAR = 1;
    /**
     * directory for file: divide by year/month
     */
    public static final int DIRECTORY_TYPE_MONTH = 2;
    /**
     * directory for file: divide by year/month/date
     */
    public static final int DIRECTORY_TYPE_DAY = 3;
    public static final String NAME_SPLITTER = "/";

    private String endpoint, accessKeyId, accessKeySecret, bucketName;

    private int directoryType = DIRECTORY_TYPE_NONE;

    private boolean divideByClient;

    private OSSClientBuilder ossClientBuilder;

    public AliOssFileRepository(AliOssConfig aliOssConfig, int directoryType, boolean divideByClient) {
        this.endpoint = aliOssConfig.getEndpoint();
        this.accessKeyId = aliOssConfig.getAccessKeyId();
        this.accessKeySecret = aliOssConfig.getAccessKeySecret();
        this.bucketName = aliOssConfig.getBucketName();
        this.directoryType = directoryType;
        this.divideByClient = divideByClient;
        ossClientBuilder = new OSSClientBuilder();
    }

    /**
     * Constructor
     * file divide by client
     * @param aliOssConfig
     */
    public AliOssFileRepository(AliOssConfig aliOssConfig) {
        this(aliOssConfig, DIRECTORY_TYPE_NONE, true);
    }

    private String adjustDate(int date) {
        if (date < 10) {
            return "0" + date;
        } else {
            return String.valueOf(date);
        }
    }

    @Override
    protected String generateFileId(String clientId) {
        StringBuilder sb = new StringBuilder();
        if (divideByClient) {
            sb.append(clientId).append(NAME_SPLITTER);
        }
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

    @Override
    protected <T extends FileMetaInf> void saveFile(String fileId, InputStream inputStream, T fileMetaInf)
            throws Throwable {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setObjectTagging(ObjectTaggingHelper.toTags(fileMetaInf));
            ossClient.putObject(bucketName, fileId, inputStream, objectMetadata);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void get(String fileId, long offset, int length, OutputStream outputStream) throws Throwable {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fileId);
            if (ossObject == null) {
                throw new RuntimeException("OSS object not found.");
            }
            byte[] buff = new byte[4 * 1024];
            int len = 0;
            InputStream inputStream = ossObject.getObjectContent();
            try {
                long size = ossObject.getObjectMetadata().getContentLength();
                if (length > 0) {
                    size = Math.min(size, length);
                }
                if (offset > 0) {
                    inputStream.skip(offset);
                }
                long restSize = size;
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
    public <T extends FileMetaInf> T getMetaInf(String fileId, Class<T> clazz) throws Throwable {
        OSS ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        try {
            TagSet tagSet = ossClient.getObjectTagging(bucketName, fileId);
            return ObjectTaggingHelper.parseTags(tagSet, clazz);
        } finally {
          ossClient.shutdown();
        }
    }
}
