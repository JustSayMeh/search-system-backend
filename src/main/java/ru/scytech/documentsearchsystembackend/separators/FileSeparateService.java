package ru.scytech.documentsearchsystembackend.separators;

import ru.scytech.documentsearchsystembackend.model.FileData;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileSeparateService {
    public List<FileData> separateFile(String filename, String domain, MediaType type, byte[] bytes) throws IOException {
        FileSeparator fileSeparator = createFileSeparator(type, bytes);
        List<byte[]> pageBytes = fileSeparator.getPagesBytes();
        List<FileData> fileDataList = new ArrayList<>();
        for (int i = 0; i < pageBytes.size(); i++) {
            FileData fileData = new FileData(domain + "/" + filename + "/" + (i + 1), pageBytes.get(i), type);
            fileDataList.add(fileData);
        }
        return fileDataList;
    }

    public FileSeparator createFileSeparator(MediaType type, byte[] bytes) throws IOException {
        if (type.equals(MediaType.APPLICATION_PDF)) {
            return new PDFSeparator(bytes);
        } else {
            return new DefaultSeparator(bytes);
        }
    }
}
