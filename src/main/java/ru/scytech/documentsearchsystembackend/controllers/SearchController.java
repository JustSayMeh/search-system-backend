package ru.scytech.documentsearchsystembackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.scytech.documentsearchsystembackend.model.QueryDTO;
import ru.scytech.documentsearchsystembackend.model.results.PhraseSuggestResult;
import ru.scytech.documentsearchsystembackend.model.results.SearchResult;
import ru.scytech.documentsearchsystembackend.services.SearchSystemFacade;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController {
    private SearchSystemFacade searchService;

    public SearchController(SearchSystemFacade searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<? extends SearchResult>> searchPhrase(@RequestBody QueryDTO queryDTO) throws IOException {
        var searchResult = searchService
                .searchIntoPageContent(queryDTO).stream()
                .sorted((a, b) ->
                        Float.compare(b.getScore(), a.getScore())
                )
                .collect(Collectors.toList());
        return ResponseEntity.ok(searchResult);
    }

    @PostMapping(value = "/suggest")
    public ResponseEntity<List<PhraseSuggestResult>> suggestPhrase(@RequestBody QueryDTO queryDTO) throws IOException {
        var result = searchService
                .suggestIntoPageContent(queryDTO.getQuery());
        return ResponseEntity.ok(result);
    }
}
