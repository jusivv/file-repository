package org.coodex.filerepository.ext.crypto;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Objects;

public class EncryptInputStream extends InputStream {
    private static Logger log = LoggerFactory.getLogger(EncryptInputStream.class);

    private static final int BUFFER_SIZE = 4 * 1024;
    private CryptoCipher cryptoCipher;
    private InputStream inputStream;

    private ByteBuffer plainTextBuffer, cipherTextBuffer;
    private byte[] buff;
    private boolean closed;

    private final byte[] oneByteBuf = new byte[1];

    public EncryptInputStream(InputStream inputStream, byte[] key, CryptoParameter cryptoParameter) throws IOException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        this.inputStream = inputStream;
        this.cryptoCipher = Utils.getCipherInstance(cryptoParameter.getTransformation(),
                cryptoParameter.getProperties());
        this.cryptoCipher.init(Cipher.ENCRYPT_MODE, cryptoParameter.getKey(key),
                cryptoParameter.getParameterSpec(CryptoParameter.DEFAULT_INITIALIZATION_VECTOR));
        this.plainTextBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.cipherTextBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.cipherTextBuffer.flip();
        this.buff = new byte[BUFFER_SIZE];
        this.closed = false;
    }

    private void checkStream() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    private void loadBuffer() throws IOException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (!cipherTextBuffer.hasRemaining()) {
            plainTextBuffer.clear();
            cipherTextBuffer.clear();
            int len;
            while ((len = inputStream.read(buff)) == 0) {
                /* no op */
            }
            log.debug("load buffer size: {}", len);
            if (len > 0) {
                plainTextBuffer.put(buff, 0, len);
                plainTextBuffer.flip();
                cryptoCipher.update(plainTextBuffer, cipherTextBuffer);
            } else if (len < 0) {
                plainTextBuffer.flip();
                cryptoCipher.doFinal(plainTextBuffer, cipherTextBuffer);
            }
            cipherTextBuffer.flip();
        }
    }

    @Override
    public int read() throws IOException {
        int n = this.read(oneByteBuf, 0, 1);
        return (n == -1) ? -1 : oneByteBuf[0] & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkStream();
        Objects.requireNonNull(b, "byte array");
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        try {
            loadBuffer();
            if (cipherTextBuffer.hasRemaining()) {
                int n = Math.min(len, cipherTextBuffer.remaining());
                cipherTextBuffer.get(b, off, n);
                return n;
            } else {
                return -1;
            }
        } catch (ShortBufferException e) {
            throw new IOException(e);
        } catch (IllegalBlockSizeException e) {
            throw new IOException(e);
        } catch (BadPaddingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            plainTextBuffer.clear();
            cipherTextBuffer.clear();
            inputStream.close();
            cryptoCipher.close();
            closed = true;
        }
    }
}
