package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.file.repository.alioss.AliOssFileRepository;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.sample.conf.SampleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AliOssSample implements IFileRespositorySample {
    private static Logger log = LoggerFactory.getLogger(AliOssSample.class);

    private IFileRepository fileRepository;

    private SampleConfig config;

    @Override
    public void build(SampleConfig config) {
        this.config = config;
        this.fileRepository = new AliOssFileRepository(config.getAliOss());
    }

    @Override
    public String saveFile() throws Throwable {
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
            log.debug("file saved, id: {}", fileId);
            return fileId;
        } finally {
            is.close();
        }
    }

    @Override
    public void readFile(String fileId) throws Throwable {
        FileMetaInf metaInf = fileRepository.getMetaInf(fileId);
        log.debug("file meta-inf: {}", JSON.toJSONString(metaInf));
        String outputPath = config.getOutput();
        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }
        FileOutputStream outputStream = new FileOutputStream(outputPath + metaInf.getFileName() + "."
                + metaInf.getExtName());
        try {
            fileRepository.get(fileId, outputStream);
            log.debug("read file to {}", outputPath);
        } finally {
            outputStream.close();
        }
    }

    @Override
    public void deleteFile(String fileId) throws Throwable {
        fileRepository.delete(fileId);
        log.debug("file deleted, id: {}", fileId);
    }
}
