package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.filerepository.local.LocalRepositoryPath;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CtrCryptoAccessSample {
    private static Logger log = LoggerFactory.getLogger(CtrCryptoAccessSample.class);

    private static Profile config = Profile.get("config.properties");

    public static void main(String[] args) throws Throwable {
        String[] paths = config.getStrList("file.repository.path.list");
        List<LocalRepositoryPath> pathList = new ArrayList<>();
        for (String pathName : paths) {
            String bathPath = config.getString("file.repository.path." + pathName);
            pathList.add(new LocalRepositoryPath(
                    config.getString("file.repository.path." + pathName),
                    config.getBool("file.repository.path." + pathName + ".read", true),
                    config.getBool("file.repository.path." + pathName + ".write", true)
            ));
        }
        IFileRepository fileRepository = new LocalFileRepository(
                pathList.toArray(new LocalRepositoryPath[0]),
                new HashPathGenerator());
        String fileId = saveFile(fileRepository);
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

        getFile(fileId, fileMetaInf.getFileName() + "." + fileMetaInf.getExtName(), fileRepository);
        log.info("get file, id: {}", fileId);
        log.info("continue ? (y/n)");
        i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }

        deleteFile(fileId, fileRepository);
        log.info("store file deleted, fileId: {}", fileId);
    }

    private static String saveFile(IFileRepository fileRepository) throws Throwable {
        File file = new File(config.getString("sample.save.file"));
        FileMetaInf fileMetaInf = new FileMetaInf();
        fileMetaInf.setClientId(CtrCryptoAccessSample.class.getSimpleName());
        String fn = file.getName();
        fileMetaInf.setFileName(fn.substring(0, fn.lastIndexOf('.')));
        fileMetaInf.setExtName(fn.substring(fn.lastIndexOf('.') + 1));
        fileMetaInf.setFileSize(file.length());
        InputStream inputStream = new FileInputStream(file);
        try {
//            return fileRepository.save(fileMetaInf,
//                    new CtrCryptoWriteCallback(inputStream, Base64.getDecoder().decode(config.getString("aes.ctr.key"))));
            return fileRepository.asyncSave(inputStream, fileMetaInf, ((success, fileId, t) -> {
                try {
                    inputStream.close();
                    if (!success) {
                        log.error(t.getLocalizedMessage(), t);
                    } else {
                        log.debug("save file {} in async model", fileId);
                    }
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }));
        } finally {
//            inputStream.close();
        }
    }

    private static StoredFileMetaInf getFileMetaInf(String fileId, IFileRepository fileRepository) throws Throwable {
        return fileRepository.getMetaInf(fileId);
    }

    private static void getFile(String fileId, String fileName, IFileRepository fileRepository) throws Throwable {
        File file = new File(config.getString("sample.get.file.path") + "/" + fileName);
        File filePath = file.getParentFile();
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        try {
//            fileRepository.get(fileId,
//                    new CtrCryptoReadCallback(outputStream, Base64.getDecoder().decode(config.getString("aes.ctr.key"))));
            fileRepository.get(fileId, outputStream);
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }

    private static void deleteFile(String fileId, IFileRepository fileRepository) throws Throwable {
//        fileRepository.delete(fileId);
        fileRepository.asyncDelete(fileId, ((success, fileId1, t) -> {
            if (!success) {
                log.error(t.getLocalizedMessage(), t);
            } else {
                log.debug("delete file {} in async model", fileId1);
            }
        }));
    }
}
