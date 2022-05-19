package org.coodex.filerepository.sample;

import org.coodex.filerepository.local.LocalRepositoryPath;
import org.yaml.snakeyaml.Yaml;

import java.io.File;

public class LocalStorageSampleConfig {

    /**
     * base paths for file to save
     */
    private LocalRepositoryPath[] paths;
    /**
     * AES key
     */
    private String aesKey;
    /**
     * input file for test
     */
    private String file;
    /**
     * output directory
     */
    private String output;

    public static LocalStorageSampleConfig loadFrom(String yamlFileName) {
        Yaml yaml = new Yaml();
        LocalStorageSampleConfig config = yaml.loadAs(
                LocalStorageSample.class.getClassLoader().getResourceAsStream(yamlFileName),
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
