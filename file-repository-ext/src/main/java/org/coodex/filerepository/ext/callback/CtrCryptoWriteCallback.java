package org.coodex.filerepository.ext.callback;

import org.apache.commons.crypto.stream.CtrCryptoOutputStream;
import org.coodex.filerepository.api.RepositoryWriteCallback;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

public class CtrCryptoWriteCallback implements RepositoryWriteCallback {
    private static Logger log = LoggerFactory.getLogger(CtrCryptoWriteCallback.class);

    private InputStream inputStream;

    private Key key;

    public CtrCryptoWriteCallback(InputStream inputStream, byte[] key) {
        this.inputStream = inputStream;
        this.key = new SecretKeySpec(key, CtrCryptoParameter.TRANSFORMATION);
    }

    @Override
    public int write(OutputStream outputStream) {
        try {
            CtrCryptoOutputStream ctrCryptoOutputStream = new CtrCryptoOutputStream(CtrCryptoParameter.getProperties(),
                    outputStream, key.getEncoded(), CtrCryptoParameter.IV.getIV());
            try {
                Common.copyStream(inputStream, ctrCryptoOutputStream);
                return 0;
            } finally {
                ctrCryptoOutputStream.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return 1;
        }
    }
}
