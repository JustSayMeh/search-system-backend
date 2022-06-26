package ru.scytech.documentsearchsystembackend.controllers;

import ru.scytech.documentsearchsystembackend.model.QueryDTO;
import ru.scytech.documentsearchsystembackend.model.SearchResult;
import ru.scytech.documentsearchsystembackend.services.DefaultSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController {
    private DefaultSearchService searchService;

    public SearchController(DefaultSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> searchPhrase(@RequestBody QueryDTO queryDTO) throws IOException {
        var searchResult = searchService
                .search(queryDTO.getQuery()).stream().sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(searchResult);
    }
}
