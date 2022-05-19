package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.file.repository.alioss.AliOssFileRepository;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class AliOssSample {
    private static Logger log = LoggerFactory.getLogger(AliOssSample.class);

    public static void main(String[] args) throws Throwable {
        SampleConfig config = SampleConfig.loadFrom("local-storage-sample.yml");
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        String accessKeyId = "";
        String accessKeySecret = "";
        String bucketName = "etollpay";
        IFileRepository fileRepository = new AliOssFileRepository(config.getAliOss().getEndpoint(),
                config.getAliOss().getAccessKeyId(), config.getAliOss().getAccessKeySecret(),
                config.getAliOss().getBucketName(), AliOssFileRepository.DIRECTORY_TYPE_DAY);
        String fileId = testSave(fileRepository, config);
        log.debug("file id: {}", fileId);

    }

    private static String testSave(IFileRepository fileRepository, SampleConfig config) throws Throwable {
        File file = new File(config.getFile());
        String fileName = file.getName();
        int lastIndex = fileName.lastIndexOf(".");
        FileMetaInf fileMetaInf = new FileMetaInf();
        fileMetaInf.setFileName(fileName.substring(0, lastIndex));
        fileMetaInf.setExtName(fileName.substring(lastIndex + 1));
        fileMetaInf.setFileSize(file.length());
        fileMetaInf.setClientId("test");
        InputStream is = new FileInputStream(file);
        try {
            String fileId = fileRepository.save(is, fileMetaInf);

            return fileId;
        } finally {
            is.close();
        }
    }

    private static void testGetTagging(String fileId, IFileRepository fileRepository) throws Throwable {
        StoredFileMetaInf storedFileMetaInf = fileRepository.getMetaInf(fileId);
        log.debug("stored meta-inf: {}", JSON.toJSONString(storedFileMetaInf));
    }

    private static void testGet(String fileId, IFileRepository fileRepository, SampleConfig config) throws Throwable {
        StoredFileMetaInf storedFileMetaInf = fileRepository.getMetaInf(fileId);
        log.debug("stored meta-inf: {}", JSON.toJSONString(storedFileMetaInf));

    }
}
