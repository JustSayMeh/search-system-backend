package ru.scytech.documentsearchsystembackend.controllers;

import ru.scytech.documentsearchsystembackend.model.QueryDTO;
import ru.scytech.documentsearchsystembackend.services.DefaultSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SearchController {
    private DefaultSearchService searchService;
    public SearchController(DefaultSearchService searchService) {
        this.searchService = searchService;
    }
    @GetMapping("/search")
    public ResponseEntity searchPhrase(@RequestBody QueryDTO queryDTO) throws IOException {
        var searchResult = searchService.search(queryDTO.getQuery());
        return ResponseEntity.ok(searchResult);
    }
}
