package org.coodex.filerepository.sample;

import org.coodex.filerepository.sample.conf.SampleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class SampleExecutor {
    private static Logger log = LoggerFactory.getLogger(SampleExecutor.class);

    public static void main(String[] args) throws Throwable {
        // file repository sample instance
//        IFileRespositorySample sample = new LocalStorageSample();
        IFileRespositorySample sample = new CtrCryptoAccessSample();
//        IFileRespositorySample sample = new AliOssSample();
        SampleConfig config = SampleConfig.loadFrom("local-storage-sample.yml");
        sample.build(config);
        // save file
        String fileId = sample.saveFile();
        log.info("file saved, id: {}", fileId);
        Scanner input = new Scanner(System.in);
        log.info("read file, continue ? (y/n)");
        String i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }
        // read file
        sample.readFile(fileId);
        log.info("finish reading file, id: {}", fileId);
        log.info("delete file, continue ? (y/n)");
        i = input.next();
        if (!i.toLowerCase().equals("y")) {
            return;
        }
        // delete file
        sample.deleteFile(fileId);
        log.info("store file deleted, fileId: {}", fileId);
    }
}
