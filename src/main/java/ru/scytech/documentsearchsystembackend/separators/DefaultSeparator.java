package ru.scytech.documentsearchsystembackend.separators;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DefaultSeparator implements FileSeparator {
    private byte[] bytes;

    public DefaultSeparator(byte[] bytes) throws IOException {
        this.bytes = bytes;
    }

    @Override
    public List<byte[]> getPagesBytes() throws IOException {
        return List.of(bytes);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public List<String> getKeywords() {
        return Collections.emptyList();
    }
}
