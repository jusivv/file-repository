package org.coodex.filerepository.local;

import java.io.File;

/**
 * file path by hash value
 */
public class HashPathGenerator implements IPathGenerator {

    @Override
    public String getPath(String seed) {
        int hash = BKDRHash(seed);
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < 4; ++i) {
            String hex = ((hash & 240) == 0 ? "0" : "") + Integer.toHexString(hash & 255);
            hash >>>= 8;
            path.append(File.separatorChar).append(hex);
        }
        path.append(File.separatorChar);
        return path.toString();
    }

    private int BKDRHash(String str) {
        if (str == null) {
            return 0;
        }
        int seed = 131;
        int hash = 0;
        byte[] buf = str.getBytes();
        for (byte b : buf) {
            hash = hash * seed + (int) b;
        }
        return hash & 0x7FFFFFFF;
    }
}
