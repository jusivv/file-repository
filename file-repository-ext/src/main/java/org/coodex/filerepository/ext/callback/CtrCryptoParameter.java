package org.coodex.filerepository.ext.callback;

import org.apache.commons.crypto.cipher.CryptoCipherFactory;

import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.Charset;
import java.util.Properties;

public class CtrCryptoParameter {
    public static final String TRANSFORMATION = "AES/CTR/NoPadding";

    public static final IvParameterSpec IV = new IvParameterSpec("0123456789ABCDEF".getBytes(Charset.forName("UTF-8")));

    private static Properties properties = new Properties();

    static {
        properties.setProperty(CryptoCipherFactory.CLASSES_KEY,
                CryptoCipherFactory.CipherProvider.OPENSSL.getClassName());
    }

    public static Properties getProperties() {
        return properties;
    }
}
