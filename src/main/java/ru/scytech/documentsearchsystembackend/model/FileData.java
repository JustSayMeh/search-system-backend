package ru.scytech.documentsearchsystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class FileData {
    private String fileName;
    private byte[] content;
    private MediaType type;
}
