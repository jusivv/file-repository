package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.filerepository.local.LocalRepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Scanner;

public class LocalStorageSample {
    private static Logger log = LoggerFactory.getLogger(LocalStorageSample.class);

    public static void main(String[] args) throws Throwable {
        LocalStorageSampleConfig config = loadConfig();
        LocalFileRepository fileRepository = new LocalFileRepository(config.getPaths(), new HashPathGenerator());
        String fileId = saveFile(fileRepository, config);
        log.info("file saved, id: {}", fileId);
        Scanner input = new Scanner(System.in);
        log.info("continue ? (y/n)");
        String i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }
        StoredFileMetaInf fileMetaInf = fileRepository.getMetaInf(fileId);
        log.info("file meta-inf: {}", JSON.toJSONString(fileMetaInf));
        log.info("continue ? (y/n)");
        i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }

        getFile(fileId, fileMetaInf.getFileName() + "." + fileMetaInf.getExtName(), 0L, 1024 * 3,
                fileRepository, config);
        log.info("get file, id: {}", fileId);
        log.info("continue ? (y/n)");
        i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }

        deleteFile(fileId, fileRepository);
        log.info("store file deleted, fileId: {}", fileId);
    }

    private static LocalStorageSampleConfig loadConfig() {
        Yaml yaml = new Yaml();
        LocalStorageSampleConfig config = yaml.loadAs(
                LocalStorageSample.class.getClassLoader().getResourceAsStream("local-storage-sample.yml"),
                LocalStorageSampleConfig.class);
        for (LocalRepositoryPath path : config.getPaths()) {
            File basePath = new File(path.getLocation());
            if (!basePath.exists()) {
                basePath.mkdirs();
            }
        }
        File outputDir = new File(config.getOutput());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        return config;
    }

    private static String saveFile(IFileRepository fileRepository, LocalStorageSampleConfig config) throws Throwable {
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

    private static void getFile(String fileId, String fileName, long offset, int length, IFileRepository fileRepository,
                                LocalStorageSampleConfig config) throws Throwable {
        File file = new File(config.getOutput() + "/" + fileName);
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
