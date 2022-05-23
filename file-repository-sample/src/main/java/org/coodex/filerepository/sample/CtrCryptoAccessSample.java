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
import org.coodex.filerepository.sample.conf.SampleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;

public class CtrCryptoAccessSample implements IFileRespositorySample {
    private static Logger log = LoggerFactory.getLogger(CtrCryptoAccessSample.class);

    private static final CryptoParameter CRYPTO_PARAMETER = CryptoParameter.buildCtrCryptoParameter(
            CryptoParameter.CipherClass.JCE,
            null
    );

    private IFileRepository fileRepository;
    private SampleConfig config;

    @Override
    public void build(SampleConfig config) {
        this.config = config;
        this.fileRepository = new LocalFileRepository(config.getPaths(),
//                new DateTimePathGenerator());
                new HashPathGenerator());
    }

    @Override
    public String saveFile() throws Throwable {
        File file = new File(config.getFile());
        FileMetaInf fileMetaInf = new FileMetaInf();
        fileMetaInf.setClientId(CtrCryptoAccessSample.class.getSimpleName());
        String fn = file.getName();
        fileMetaInf.setFileName(fn.substring(0, fn.lastIndexOf('.')));
        fileMetaInf.setExtName(fn.substring(fn.lastIndexOf('.') + 1));
        fileMetaInf.setFileSize(file.length());
        InputStream inputStream = new FileInputStream(file);
        try {
            String fileId = fileRepository.save(fileMetaInf,
                    new CryptoWriteCallback(inputStream, Base64.getDecoder().decode(config.getAesKey()),
                            CRYPTO_PARAMETER));
            log.debug("file saved, id: {}", fileId);
            return fileId;
        } finally {
            inputStream.close();
        }
    }

    @Override
    public void readFile(String fileId) throws Throwable {
        StoredFileMetaInf fileMetaInf = fileRepository.getMetaInf(fileId);
        log.info("file meta-inf: {}", JSON.toJSONString(fileMetaInf));
        String outputFile = config.getOutput()
                + (config.getOutput().endsWith(File.separator) ? "" : File.separator)
                + fileMetaInf.getFileName() + "." + fileMetaInf.getExtName();
        OutputStream outputStream = new FileOutputStream(outputFile);
        CryptoReadCallback readCallback = new CryptoReadCallback(outputStream,
                Base64.getDecoder().decode(config.getAesKey()),
                CRYPTO_PARAMETER);
        try {
            fileRepository.get(fileId, readCallback);
            log.debug("read file to: {}", outputFile);
        } finally {
            readCallback.finished();
            outputStream.flush();
            outputStream.close();
        }
    }

    @Override
    public void deleteFile(String fileId) throws Throwable {
        fileRepository.delete(fileId);
        log.debug("file deleted, id: {}", fileId);
    }
}
