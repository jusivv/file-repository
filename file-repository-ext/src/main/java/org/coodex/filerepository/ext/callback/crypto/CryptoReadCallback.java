package org.coodex.filerepository.ext.callback.crypto;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.coodex.filerepository.api.RepositoryReadCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class CryptoReadCallback implements RepositoryReadCallback {
    private static Logger log = LoggerFactory.getLogger(CryptoReadCallback.class);

    private CryptoCipher cryptoCipher;

    private OutputStream outputStream;

    public CryptoReadCallback(OutputStream outputStream, byte[] key, CryptoParameter cryptoParameter) throws IOException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        this.outputStream = outputStream;
        cryptoCipher = Utils.getCipherInstance(cryptoParameter.getTransformation(), cryptoParameter.getProperties());
        cryptoCipher.init(Cipher.DECRYPT_MODE, cryptoParameter.getKey(key),
                cryptoParameter.getParameterSpec(CryptoParameter.DEFAULT_INITIALIZATION_VECTOR));
    }

    @Override
    public void read(byte[] buff, int len, long fileSize) throws Throwable {
        byte[] outBuff = new byte[len];
        cryptoCipher.update(buff, 0, len, outBuff, 0);
        outputStream.write(outBuff, 0, len);
    }

    public void finished() {
        if (cryptoCipher != null) {
            try {
                cryptoCipher.close();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }
}
