package org.coodex.filerepository.ext.crypto;

import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.coodex.filerepository.api.RepositoryWriteCallback;
import org.coodex.util.Common;

import java.io.InputStream;
import java.io.OutputStream;

public class CryptoWriteCallback implements RepositoryWriteCallback {

    private InputStream inputStream;

    private byte[] key;

    private CryptoParameter cryptoParameter;

    public CryptoWriteCallback(InputStream inputStream, byte[] key, CryptoParameter cryptoParameter) {
        this.inputStream = inputStream;
        this.key = key;
        this.cryptoParameter = cryptoParameter;
    }

    @Override
    public void write(OutputStream outputStream) throws Throwable {
        CryptoOutputStream cryptoOutputStream = new CryptoOutputStream(cryptoParameter.getTransformation(),
                cryptoParameter.getProperties(), outputStream, cryptoParameter.getKey(key),
                cryptoParameter.getParameterSpec(CryptoParameter.DEFAULT_INITIALIZATION_VECTOR));
        try {
            Common.copyStream(inputStream, cryptoOutputStream);
        } finally {
            cryptoOutputStream.close();
        }
    }
}
