package org.coodex.filerepository.local;

/**
 * file path generator
 */
public interface IPathGenerator {
    /**
     * get file path
     * @param seed  parameter for generator
     * @return      file path
     */
    String getPath(String seed);
}
