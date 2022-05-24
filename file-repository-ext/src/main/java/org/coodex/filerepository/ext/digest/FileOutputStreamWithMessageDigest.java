package org.coodex.filerepository.ext.digest;

import org.coodex.util.Common;

import java.io.*;
import java.security.MessageDigest;

public class FileOutputStreamWithMessageDigest extends OutputStream {

    private FileOutputStream fileOutputStream;
    private MessageDigest digest;
    private String digestValueHex;

    public FileOutputStreamWithMessageDigest(File file, MessageDigest digest) throws FileNotFoundException {
        this.fileOutputStream = new FileOutputStream(file);
        this.digest = digest;
    }

    @Override
    public void write(int b) throws IOException {
        fileOutputStream.write(b);
        if (digest != null) {
            digest.update((byte) b);
        }
    }

    @Override
    public void flush() throws IOException {
        fileOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        fileOutputStream.close();
        if (digest != null) {
            digestValueHex = Common.byte2hex(digest.digest());
        }
    }

    public String getDigestValue() {
        return digestValueHex;
    }
}
