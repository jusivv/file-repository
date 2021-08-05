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
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        String accessKeyId = "";
        String accessKeySecret = "";
        String bucketName = "etollpay";
        IFileRepository fileRepository = new AliOssFileRepository(endpoint, accessKeyId, accessKeySecret, bucketName,
                AliOssFileRepository.DIRECTORY_TYPE_DAY);
//        testSave(fileRepository);
        testGetTagging(fileRepository);
    }

    private static void testSave(IFileRepository fileRepository) throws Throwable {
        File file = new File("/Users/sujiwu/Downloads/aliyun-oss-java-sdk-demo-mvn-3.10.2.zip");
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
            log.debug("file id: {}", fileId);
        } finally {
            is.close();
        }
    }

    private static void testGetTagging(IFileRepository fileRepository) throws Throwable {
        String fileId = "2021/08/05/63a62922560c439891a330368aa16b62";
        StoredFileMetaInf storedFileMetaInf = fileRepository.getMetaInf(fileId);
        log.debug("stored meta-inf: {}", JSON.toJSONString(storedFileMetaInf));
    }
}
