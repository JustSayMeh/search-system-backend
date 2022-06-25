package ru.scytech.documentsearchsystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResult {
    private String fileName;
    private String domain;
    private int page;
    private List<Highlight> highlightList;
    private float score;
}
