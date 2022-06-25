package ru.scytech.documentsearchsystembackend.services.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface FileSystemService {
    void saveFile(String fileName, String domain, byte[] bytes) throws IOException;

    InputStream getFile(String fileName, String domain) throws FileNotFoundException;

    boolean isFileExist(String fileName, String domain);

    Optional<String> getFileContentType(String filename, String domain) throws IOException;
}
