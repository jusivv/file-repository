package org.coodex.filerepository.ext.callback.crypto;

import org.apache.commons.crypto.cipher.CryptoCipherFactory;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Properties;

public class CryptoParameter {

    public static final byte[] DEFAULT_INITIALIZATION_VECTOR = new byte[]{
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46
    };

    public enum CipherClass {
        JCE, OPENSSL
    }

    public static CryptoParameter build(String transformation, String algorithm, CipherClass cipherClass,
                                        String jceProviderName) {
        CryptoParameter cryptoParameter = new CryptoParameter();
        cryptoParameter.transformation = transformation;
        cryptoParameter.algorithm = algorithm;
        switch (cipherClass) {
            case JCE:
                cryptoParameter.properties.setProperty(CryptoCipherFactory.CLASSES_KEY,
                        CryptoCipherFactory.CipherProvider.JCE.getClassName());
                if (jceProviderName != null && !jceProviderName.trim().isEmpty()) {
                    cryptoParameter.properties.setProperty(CryptoCipherFactory.JCE_PROVIDER_KEY, jceProviderName);
                }
                break;
            case OPENSSL:
                cryptoParameter.properties.setProperty(CryptoCipherFactory.CLASSES_KEY,
                        CryptoCipherFactory.CipherProvider.OPENSSL.getClassName());
                break;
        }
        return cryptoParameter;
    }

    public static CryptoParameter buildCtrCryptoParameter(CipherClass cipherClass, String jceProviderName) {
        return build("AES/CTR/NoPadding", "AES", cipherClass, jceProviderName);
    }

    public static CryptoParameter buildCfbCryptoParameter(CipherClass cipherClass, String jceProviderName) {
        return build("AES/CFB/NoPadding", "AES", cipherClass, jceProviderName);
    }

    private String transformation;

    private Properties properties;

    private String algorithm;

    public CryptoParameter() {
        properties = new Properties();
    }

    public String getTransformation() {
        return transformation;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Key getKey(byte[] key) {
        return new SecretKeySpec(key, getAlgorithm());
    }

    public AlgorithmParameterSpec getParameterSpec(byte[] iv) {
        return new IvParameterSpec(iv);
    }

    public Properties getProperties() {
        return properties;
    }

}
