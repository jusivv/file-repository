package org.coodex.filerepository.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigPathGenerator implements IPathGenerator {
    private static Logger log = LoggerFactory.getLogger(ConfigPathGenerator.class);

    private static final String FILE_PATH_CONFIG_NAME = "file.repository.path";

    private static final String DEFAULT_FILE_PATH = "/file_repository/";

    @Override
    public String getPath(String seed) {
        if (seed != null) {
            String propPath = this.getClass().getClassLoader().getResource(seed).getPath();
            Properties properties = new Properties();
            File f = new File(propPath);
            if (f.isFile()) {
                try {
                    properties.load(new FileInputStream(f));
                    return properties.getProperty(FILE_PATH_CONFIG_NAME, DEFAULT_FILE_PATH);
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return DEFAULT_FILE_PATH;
    }
}
