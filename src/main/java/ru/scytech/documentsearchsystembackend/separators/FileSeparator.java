package ru.scytech.documentsearchsystembackend.separators;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface FileSeparator extends Closeable {
    List<byte[]> getPagesBytes() throws IOException;

    String getTitle();

    List<String> getKeywords();
}
