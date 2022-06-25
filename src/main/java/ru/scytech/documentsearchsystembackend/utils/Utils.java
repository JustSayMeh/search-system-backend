package ru.scytech.documentsearchsystembackend.utils;

import ru.scytech.documentsearchsystembackend.model.Highlight;
import ru.scytech.documentsearchsystembackend.model.SearchResult;

import java.util.List;

public class Utils {
    public static SearchResult createSearchResultFromRaw(String fileNameRaw, List<Highlight> highlightList, float score) {
        String[] fileNameParts = fileNameRaw.split("/");
        String domain = fileNameParts[0];
        String fileName = fileNameParts[1];
        int page = Integer.parseInt(fileNameParts[2]);
        return new SearchResult(fileName, domain, page, highlightList, score);
    }
}
