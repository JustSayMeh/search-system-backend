package ru.scytech.documentsearchsystembackend.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.scytech.documentsearchsystembackend.services.DefaultDocumentAccessService;
import ru.scytech.documentsearchsystembackend.services.SearchSystemFacade;

import javax.naming.OperationNotSupportedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/docs")
public class FilesController {
    private SearchSystemFacade searchSystemFacade;
    private DefaultDocumentAccessService defaultFileSystemService;

    public FilesController(SearchSystemFacade searchSystemFacade, DefaultDocumentAccessService defaultFileSystemService) {
        this.searchSystemFacade = searchSystemFacade;
        this.defaultFileSystemService = defaultFileSystemService;
    }

    @GetMapping("/{domain}/{filename}")
    public ResponseEntity<Resource> checkDomainPermissionsBeforeDownloadDoc(
            @PathVariable String domain,
            @PathVariable String filename) throws IOException {

        var fileModel = searchSystemFacade.loadDoc(domain, filename);
        byte[] bytes = fileModel.getData();
        InputStreamResource inputStreamResource = new InputStreamResource(
                new ByteArrayInputStream(bytes));
        return ResponseEntity
                .ok()
                .contentLength(bytes.length)
                .contentType(fileModel.getType())
                .body(inputStreamResource);
    }

    @PostMapping("/{domain}/{filename}")
    public ResponseEntity checkAdminPermissionsBeforeUploadDoc(
            @PathVariable String domain,
            @PathVariable String filename,
            @RequestParam("file") MultipartFile file,
            @RequestParam("tags") List<String> tags) throws IOException {
        InputStream fileInputStream = file.getInputStream();
        try {
            searchSystemFacade.indexDoc(filename, domain, tags, fileInputStream);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PutMapping("{domain}/{filename}")
    public ResponseEntity checkAdminPermissionsBeforeUpdateDoc(
            @PathVariable String domain,
            @PathVariable String filename,
            @RequestParam("file") MultipartFile file,
            @RequestParam("tags") List<String> tags) throws IOException {
        InputStream fileInputStream = file.getInputStream();
        try {
            searchSystemFacade.updateDoc(domain, filename, tags, fileInputStream);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{domain}/{filename}")
    public ResponseEntity checkAdminPermissionsBeforeDeleteDoc(
            @PathVariable String domain,
            @PathVariable String filename) throws IOException {
        try {
            searchSystemFacade.deleteDoc(domain, filename);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping(value = "/title/image/{domain}/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] checkDomainPermissionsBeforeGetTitleImage(
            @PathVariable String domain,
            @PathVariable String filename) throws IOException, OperationNotSupportedException {
        return searchSystemFacade.getTitleImage(domain, filename);
    }

    @GetMapping("/domains")
    public List<String> checkDomainPermissionsAfterGetDomains() throws IOException {
        return defaultFileSystemService.loadDocsRepository(false).keySet().stream().collect(Collectors.toList());
    }
}
