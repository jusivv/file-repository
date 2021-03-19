package org.coodex.filerepository.sample;

import com.alibaba.fastjson.JSON;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.coodex.filerepository.ext.callback.CtrCryptoReadCallback;
import org.coodex.filerepository.ext.callback.CtrCryptoWriteCallback;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Base64;

public class CtrCryptoAccessSample {
    private static Logger log = LoggerFactory.getLogger(CtrCryptoAccessSample.class);

    private static Profile config = Profile.get("config.properties");

    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException {
        IFileRepository fileRepository = new LocalFileRepository(
                config.getString("file.repository.path", "/Users/sujiwu/Downloads/temp/"),
                new HashPathGenerator());
        String fileId = saveFile(fileRepository);
        log.debug("file id: {}", fileId);
        StoredFileMetaInf fileMetaInf = fileRepository.getMetaInf(fileId);
        log.debug("file meta-inf: {}", JSON.toJSONString(fileMetaInf));
        getFile(fileId, fileMetaInf.getFileName() + "." + fileMetaInf.getExtName(), fileRepository);
        deleteFile(fileId, fileRepository);
    }

    private static String saveFile(IFileRepository fileRepository) throws IOException {
        File file = new File(config.getString("sample.save.file"));
        FileMetaInf fileMetaInf = new FileMetaInf();
        fileMetaInf.setClientId(CtrCryptoAccessSample.class.getSimpleName());
        String fn = file.getName();
        fileMetaInf.setFileName(fn.substring(0, fn.lastIndexOf('.')));
        fileMetaInf.setExtName(fn.substring(fn.lastIndexOf('.') + 1));
        fileMetaInf.setFileSize(file.length());
        InputStream inputStream = new FileInputStream(file);
        try {
            return fileRepository.save(fileMetaInf,
                    new CtrCryptoWriteCallback(inputStream, Base64.getDecoder().decode(config.getString("aes.ctr.key"))));
        } finally {
            inputStream.close();
        }
    }

    private static StoredFileMetaInf getFileMetaInf(String fileId, IFileRepository fileRepository) {
        return fileRepository.getMetaInf(fileId);
    }

    private static void getFile(String fileId, String fileName, IFileRepository fileRepository) throws IOException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        File file = new File(config.getString("sample.get.file.path") + "/" + fileName);
        File filePath = file.getParentFile();
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        fileRepository.get(fileId,
                new CtrCryptoReadCallback(outputStream, Base64.getDecoder().decode(config.getString("aes.ctr.key"))));
    }

    private static void deleteFile(String fileId, IFileRepository fileRepository) {
        fileRepository.delete(fileId);
    }
}
