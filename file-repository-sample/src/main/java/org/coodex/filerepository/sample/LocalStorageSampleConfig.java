package org.coodex.filerepository.sample;

import org.coodex.filerepository.local.LocalRepositoryPath;

public class LocalStorageSampleConfig {
    private LocalRepositoryPath[] paths;
    private String aesKey;
    private String file;
    private String output;

    public LocalRepositoryPath[] getPaths() {
        return paths;
    }

    public void setPaths(LocalRepositoryPath[] paths) {
        this.paths = paths;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
