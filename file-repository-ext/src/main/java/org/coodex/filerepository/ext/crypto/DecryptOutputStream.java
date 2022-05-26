package org.coodex.filerepository.ext.crypto;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class DecryptOutputStream extends OutputStream {
    private static final int BUFFER_SIZE = 4 * 1024;
    private CryptoCipher cryptoCipher;
    private OutputStream outputStream;

    private ByteBuffer cipherTextBuffer, plainTextBuffer;
    private byte[] buff;
    private boolean closed;

    public DecryptOutputStream(OutputStream outputStream, byte[] key, CryptoParameter cryptoParameter) throws IOException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        this.outputStream = outputStream;
        this.cryptoCipher = Utils.getCipherInstance(cryptoParameter.getTransformation(),
                cryptoParameter.getProperties());
        this.cryptoCipher.init(Cipher.DECRYPT_MODE, cryptoParameter.getKey(key),
                cryptoParameter.getParameterSpec(CryptoParameter.DEFAULT_INITIALIZATION_VECTOR));
        this.cipherTextBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.plainTextBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.buff = new byte[BUFFER_SIZE];
        this.closed = false;
    }

    private void clearBuffer() throws ShortBufferException, IOException {
        cipherTextBuffer.flip();
        plainTextBuffer.clear();
        cryptoCipher.update(cipherTextBuffer, plainTextBuffer);
        plainTextBuffer.flip();
        int len = plainTextBuffer.remaining();
        plainTextBuffer.get(buff, 0, len);
        outputStream.write(buff, 0, len);
        cipherTextBuffer.clear();
        plainTextBuffer.clear();
    }

    @Override
    public void write(int b) throws IOException {
        if (!cipherTextBuffer.hasRemaining()) {
            try {
                this.clearBuffer();
            } catch (ShortBufferException e) {
                throw new IOException(e);
            }
        }
        cipherTextBuffer.put((byte) (b & 0xFF));
    }

    @Override
    public void flush() throws IOException {
        try {
            this.clearBuffer();
            outputStream.flush();
        } catch (ShortBufferException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            cipherTextBuffer.flip();
            plainTextBuffer.clear();
            try {
                cryptoCipher.doFinal(cipherTextBuffer, plainTextBuffer);
                plainTextBuffer.flip();
                int len = plainTextBuffer.remaining();
                plainTextBuffer.get(buff, 0, len);
                outputStream.write(buff, 0, len);
                closed = true;
            } catch (ShortBufferException e) {
                throw new IOException(e);
            } catch (IllegalBlockSizeException e) {
                throw new IOException(e);
            } catch (BadPaddingException e) {
                throw new IOException(e);
            } finally {
                outputStream.close();
                cryptoCipher.close();
                cipherTextBuffer.clear();
                plainTextBuffer.clear();
            }
        }
    }
}
