package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.coodex.filerepository.ext.callback.crypto.CryptoParameter;
import org.coodex.filerepository.ext.callback.crypto.CryptoReadCallback;
import org.coodex.filerepository.ext.callback.crypto.CryptoWriteCallback;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;
import java.util.Scanner;

public class CtrCryptoAccessSample {
    private static Logger log = LoggerFactory.getLogger(CtrCryptoAccessSample.class);

    private static final CryptoParameter CRYPTO_PARAMETER = CryptoParameter.buildCtrCryptoParameter(
            CryptoParameter.CipherClass.JCE,
            null
    );

    public static void main(String[] args) throws Throwable {
        LocalStorageSampleConfig config = LocalStorageSampleConfig.loadFrom("local-storage-sample.yml");
        IFileRepository fileRepository = new LocalFileRepository(config.getPaths(),
//                new DateTimePathGenerator());
                new HashPathGenerator());
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

        getFile(fileId, fileRepository, config);
        log.info("get file, id: {}", fileId);
        log.info("continue ? (y/n)");
        i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }

        deleteFile(fileId, fileRepository);
        log.info("store file deleted, fileId: {}", fileId);
    }

    private static String saveFile(IFileRepository fileRepository, LocalStorageSampleConfig config) throws Throwable {
        File file = new File(config.getFile());
        FileMetaInf fileMetaInf = new FileMetaInf();
        fileMetaInf.setClientId(CtrCryptoAccessSample.class.getSimpleName());
        String fn = file.getName();
        fileMetaInf.setFileName(fn.substring(0, fn.lastIndexOf('.')));
        fileMetaInf.setExtName(fn.substring(fn.lastIndexOf('.') + 1));
        fileMetaInf.setFileSize(file.length());
        InputStream inputStream = new FileInputStream(file);
        try {
            return fileRepository.save(fileMetaInf,
                    new CryptoWriteCallback(inputStream, Base64.getDecoder().decode(config.getAesKey()),
                            CRYPTO_PARAMETER));
        } finally {
            inputStream.close();
        }
    }

    private static StoredFileMetaInf getFileMetaInf(String fileId, IFileRepository fileRepository) throws Throwable {
        return fileRepository.getMetaInf(fileId);
    }

    private static void getFile(String fileId, IFileRepository fileRepository,
                                LocalStorageSampleConfig config) throws Throwable {
        StoredFileMetaInf fileMetaInf = fileRepository.getMetaInf(fileId);
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
        CryptoReadCallback readCallback = new CryptoReadCallback(outputStream,
                Base64.getDecoder().decode(config.getAesKey()),
                CRYPTO_PARAMETER);
        try {
            fileRepository.get(fileId, readCallback);
        } finally {
            readCallback.finished();
            outputStream.flush();
            outputStream.close();
        }
    }

    private static void deleteFile(String fileId, IFileRepository fileRepository) throws Throwable {
        fileRepository.delete(fileId);
    }
}
