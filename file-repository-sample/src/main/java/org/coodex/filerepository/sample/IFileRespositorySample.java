package org.coodex.filerepository.sample;

import org.coodex.filerepository.sample.conf.SampleConfig;

public interface IFileRespositorySample {

    void build(SampleConfig config);

    String saveFile() throws Throwable;

    void readFile(String fileId) throws Throwable;

    void deleteFile(String fileId) throws Throwable;
}
