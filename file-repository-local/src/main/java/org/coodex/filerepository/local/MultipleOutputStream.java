package org.coodex.filerepository.local;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MultipleOutputStream extends OutputStream {
    Map<String, OutputStream> outputStreams;

    public MultipleOutputStream() {
        this.outputStreams = new HashMap<>();
    }

    public void addOutputStream(String location, OutputStream outputStream) {
        outputStreams.put(location, outputStream);
    }

    @Override
    public void write(int b) throws IOException {
        for (OutputStream outputStream : outputStreams.values()) {
            outputStream.write(b);
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream outputStream : outputStreams.values()) {
            outputStream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream outputStream : outputStreams.values()) {
            outputStream.flush();
            outputStream.close();
        }
    }

    public boolean isEmpty() {
        return outputStreams.isEmpty();
    }
}
