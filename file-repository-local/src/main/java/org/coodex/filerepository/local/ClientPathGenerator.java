package org.coodex.filerepository.local;

public class ClientPathGenerator implements IPathGenerator {
    @Override
    public String getPath(String fileId) {
        int pos = fileId.indexOf('$');
        if (pos > 0) {
            return fileId.substring(0, pos);
        } else {
            return null;
        }
    }
}
