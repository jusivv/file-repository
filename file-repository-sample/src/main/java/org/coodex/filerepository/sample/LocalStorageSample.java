package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.filerepository.sample.conf.SampleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class LocalStorageSample implements IFileRespositorySample {
    private static Logger log = LoggerFactory.getLogger(LocalStorageSample.class);

    private IFileRepository fileRepository;
    private SampleConfig config;

    @Override
    public void build(SampleConfig config) {
        this.config = config;
        this.fileRepository = new LocalFileRepository(config.getPaths(), new HashPathGenerator());
    }

    @Override
    public String saveFile() throws Throwable {
        File file = new File(config.getFile());
        FileMetaInf fileMetaInf = new FileMetaInf();
        fileMetaInf.setClientId(LocalStorageSample.class.getSimpleName());
        String fn = file.getName();
        fileMetaInf.setFileName(fn.substring(0, fn.lastIndexOf('.')));
        fileMetaInf.setExtName(fn.substring(fn.lastIndexOf('.') + 1));
        fileMetaInf.setFileSize(file.length());
        InputStream inputStream = new FileInputStream(file);
        String fileId = fileRepository.asyncSave(inputStream, fileMetaInf, ((success, fid, t) -> {
            try {
                inputStream.close();
                if (!success) {
                    log.error(t.getLocalizedMessage(), t);
                } else {
                    log.debug("save file {} in async model", fid);
                }
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }));
        log.debug("save file id: {}", fileId);
        return fileId;
    }

    @Override
    public void readFile(String fileId) throws Throwable {
        long offset = 0;
        int length = 4 * 1024;
        FileMetaInf fileMetaInf = fileRepository.getMetaInf(fileId);
        log.info("file meta-inf: {}", JSON.toJSONString(fileMetaInf));
        String outputFile = config.getOutput()
                + (config.getOutput().endsWith(File.separator) ? "" : File.separator)
                + fileMetaInf.getFileName() + "." + fileMetaInf.getExtName();
        log.debug("get file to: {}", outputFile);
        File file = new File(outputFile);
        File filePath = file.getParentFile();
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        try {
            fileRepository.get(fileId, offset, length, outputStream);
            log.debug("read file offset: {}, length: {}, fileId: {}", offset, length, fileId);
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }

    @Override
    public void deleteFile(String fileId) throws Throwable {
        fileRepository.asyncDelete(fileId, ((success, fileId1, t) -> {
            if (!success) {
                log.error(t.getLocalizedMessage(), t);
            } else {
                log.debug("delete file {} in async model", fileId1);
            }
        }));
        log.debug("delete file id: {}", fileId);
    }
}
