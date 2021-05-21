package org.coodex.filerepository.ext.callback;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.utils.Utils;
import org.coodex.filerepository.api.RepositoryReadCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Properties;

public class CtrCryptoReadCallback implements RepositoryReadCallback {
    private static Logger log = LoggerFactory.getLogger(CtrCryptoReadCallback.class);

    private CryptoCipher cryptoCipher;

    private OutputStream outputStream;

    public CtrCryptoReadCallback(OutputStream outputStream, byte[] key) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.outputStream = outputStream;
        Properties properties = new Properties();
        properties.setProperty(CryptoCipherFactory.CLASSES_KEY,
                CryptoCipherFactory.CipherProvider.OPENSSL.getClassName());
        cryptoCipher = Utils.getCipherInstance(CtrCryptoParameter.TRANSFORMATION, CtrCryptoParameter.getProperties());
        cryptoCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CtrCryptoParameter.TRANSFORMATION),
                CtrCryptoParameter.IV);
    }

    @Override
    public void read(byte[] buff, int len, long fileSize) throws Throwable {
        byte[] outBuff = new byte[buff.length];
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
