package ru.scytech.documentsearchsystembackend.services;

import ru.scytech.documentsearchsystembackend.services.interfaces.FileSystemService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class DefaultFileSystemService implements FileSystemService {
    private String rootDir;

    public DefaultFileSystemService(@Value("${application.filesystem.root}") String rootDir)
            throws IOException {
        Path path = Paths.get(rootDir);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        this.rootDir = rootDir;
    }

    @Override
    public void saveFile(String fileName, String domain, byte[] bytes) throws IOException {
        createDirectoryIfNotExists(rootDir + "/" + domain);
        try (FileOutputStream fileOutputStream = new FileOutputStream(rootDir + "/" + domain + "/" + fileName)) {
            fileOutputStream.write(bytes);
        }
    }

    @Override
    public InputStream getFile(String fileName, String domain) throws FileNotFoundException {
        return new FileInputStream(rootDir + "/" + domain + "/" + fileName);
    }

    @Override
    public boolean isFileExist(String fileName, String domain) {
        Path path = Paths.get(rootDir + "/" + fileName);
        return Files.exists(path);
    }

    @Override
    public Optional<String> getFileContentType(String filename, String domain) throws IOException {
        createDirectoryIfNotExists(rootDir + "/" + domain);
        Path path = Paths.get(rootDir + "/" + domain + "/" + filename);
        return Optional.ofNullable(Files.probeContentType(path));
    }

    private void createDirectoryIfNotExists(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
    }
}
