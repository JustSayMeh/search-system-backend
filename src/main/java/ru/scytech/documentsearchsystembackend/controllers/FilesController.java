package ru.scytech.documentsearchsystembackend.controllers;

import ru.scytech.documentsearchsystembackend.model.FileData;
import ru.scytech.documentsearchsystembackend.services.DefaultSearchService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FilesController {
    private DefaultSearchService defaultSearchService;

    public FilesController(DefaultSearchService defaultSearchService) {
        this.defaultSearchService = defaultSearchService;
    }

    @GetMapping("/download/{domain}/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String domain,
                                                 @PathVariable String filename) throws IOException {

        FileData fileModel = defaultSearchService.loadFile(domain, filename);
        InputStreamResource inputStreamResource = new InputStreamResource(
                new ByteArrayInputStream(fileModel.getContent()));
        return ResponseEntity
                .ok()
                .contentLength(inputStreamResource.contentLength())
                .contentType(fileModel.getType())
                .body(inputStreamResource);
    }

    @PostMapping("/upload/{domain}/{filename}")
    public ResponseEntity uploadFile(@PathVariable String domain,
                                     @PathVariable String filename,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam("tags") List<String> tags) throws IOException {
        InputStream fileInputStream = file.getInputStream();
        defaultSearchService.indexFile(filename, domain, tags, fileInputStream);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
