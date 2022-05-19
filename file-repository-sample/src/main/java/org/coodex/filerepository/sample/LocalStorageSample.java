package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;

public class LocalStorageSample {
    private static Logger log = LoggerFactory.getLogger(LocalStorageSample.class);

    public static void main(String[] args) throws Throwable {
        SampleConfig config = SampleConfig.loadFrom("local-storage-sample.yml");
        LocalFileRepository fileRepository = new LocalFileRepository(config.getPaths(), new HashPathGenerator());
        String fileId = saveFile(fileRepository, config);
        log.info("file saved, id: {}", fileId);
        Scanner input = new Scanner(System.in);
        log.info("continue ? (y/n)");
        String i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }

        getFile(fileId, 0L, 1024 * 3, fileRepository, config);
        log.info("get file, id: {}", fileId);
        log.info("continue ? (y/n)");
        i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }

        deleteFile(fileId, fileRepository);
        log.info("store file deleted, fileId: {}", fileId);
    }

    private static String saveFile(IFileRepository fileRepository, SampleConfig config) throws Throwable {
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

    private static void getFile(String fileId, long offset, int length, IFileRepository fileRepository,
                                SampleConfig config) throws Throwable {
        StoredFileMetaInf fileMetaInf = fileRepository.getMetaInf(fileId);
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
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }

    private static void deleteFile(String fileId, IFileRepository fileRepository) throws Throwable {
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
