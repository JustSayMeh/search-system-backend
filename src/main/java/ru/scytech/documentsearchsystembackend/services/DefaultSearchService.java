package ru.scytech.documentsearchsystembackend.services;

import ru.scytech.documentsearchsystembackend.model.FileData;
import ru.scytech.documentsearchsystembackend.model.SearchResult;
import ru.scytech.documentsearchsystembackend.separators.FileSeparateService;
import ru.scytech.documentsearchsystembackend.services.interfaces.FileSystemService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultSearchService {
    private ElasticRestClient elasticRestClient;
    private FileSystemService fileSystemService;
    private FileSeparateService fileSeparateService;

    public DefaultSearchService(ElasticRestClient elasticRestClient,
                                FileSystemService fileSystemService,
                                FileSeparateService fileSeparateService) {
        this.elasticRestClient = elasticRestClient;
        this.fileSystemService = fileSystemService;
        this.fileSeparateService = fileSeparateService;
    }

    public void indexFile(String fileName, String domain, List<String> tags, InputStream inputStream)
            throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        fileSystemService.saveFile(fileName, domain, bytes);
        Optional<String> typeOptional = fileSystemService.getFileContentType(fileName, domain);
        MediaType mediaType = toMediaType(typeOptional);
        var fileDataList = fileSeparateService.separateFile(fileName, domain, mediaType, bytes);
        elasticRestClient.indexTextFiles(fileDataList);
    }

    public FileData loadFile(String domain, String fileName) throws IOException {
        InputStream inputStream = fileSystemService.getFile(fileName, domain);
        byte[] bytes = inputStream.readAllBytes();
        Optional<String> typeOptional = fileSystemService.getFileContentType(fileName, domain);
        return new FileData(fileName, bytes, toMediaType(typeOptional));
    }

    public MediaType toMediaType(Optional<String> typeOptional) {
        return typeOptional.map(MediaType::valueOf).orElse(MediaType.TEXT_PLAIN);
    }

    public List<SearchResult> search(String phrase) throws IOException {
        return elasticRestClient.searchPhrase(phrase);
    }
}
