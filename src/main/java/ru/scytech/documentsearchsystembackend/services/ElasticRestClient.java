package ru.scytech.documentsearchsystembackend.services;


import ru.scytech.documentsearchsystembackend.model.FileData;
import ru.scytech.documentsearchsystembackend.model.Highlight;
import ru.scytech.documentsearchsystembackend.model.SearchResult;
import ru.scytech.documentsearchsystembackend.model.TaggedSubstring;
import ru.scytech.documentsearchsystembackend.utils.Utils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ElasticRestClient implements Closeable {
    private RestHighLevelClient client;
    private HighlightBuilder highlightBuilder;
    private final String CONTENT_FIELD_NAME = "attachment.content";
    private final String FILE_NAME_FIELD_NAME = "filename";
    private final String FS_INDEX_NAME = "fs_index";
    private final String HIGHLIGHTER_TYPE = "fvh";
    private final String PIPELINE_NAME = "attachment";

    public ElasticRestClient(String host, int port) {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(host, port)
        ));
        highlightBuilder = new HighlightBuilder().field(CONTENT_FIELD_NAME).highlighterType(HIGHLIGHTER_TYPE);
    }

    public List<SearchResult> searchPhrase(String phrase) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(FS_INDEX_NAME);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().query(
                        QueryBuilders
                                .matchPhraseQuery(CONTENT_FIELD_NAME, phrase)
                                .slop(2))
                        .highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<SearchResult> searchResults = new ArrayList<>();
        if (response != null) {
            for (var hit : response.getHits()) {
                var fileName = hit.getSourceAsMap().get(FILE_NAME_FIELD_NAME).toString();
                List<Highlight> highLightList = new ArrayList<>();
                var highLightFields = hit.getHighlightFields();
                if (!highLightFields.containsKey(CONTENT_FIELD_NAME)) {
                    continue;
                }
                for (var text : highLightFields.get(CONTENT_FIELD_NAME).getFragments()) {
                    highLightList.add(parseHighLight(text.string(), "em"));
                }
                searchResults.add(Utils.createSearchResultFromRaw(fileName, highLightList, hit.getScore()));
            }
        }
        return searchResults;
    }

    public List<IndexResponse> indexTextFiles(List<FileData> contents) throws IOException {
        List<IndexResponse> responses = new ArrayList<>();
        for (FileData fileData : contents) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(FS_INDEX_NAME);
            indexRequest.id(UUID.randomUUID().toString());
            indexRequest.setPipeline(PIPELINE_NAME);
            indexRequest.source(
                    FILE_NAME_FIELD_NAME,
                    fileData.getFileName(),
                    "data",
                    new String(Base64
                            .getEncoder()
                            .encode(fileData.getContent())));
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            responses.add(indexResponse);
        }
        return responses;
    }

    private Highlight parseHighLight(String highlightString, String tag) {
        List<TaggedSubstring> matchedSubstrings = new ArrayList<>();
        StringBuilder filteredHighlightStringBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile(String.format("(<%s>(.*?)</%s>)", tag, tag));
        int previousGroupEnd = 0;
        Matcher matcher = pattern.matcher(highlightString);
        while (matcher.find()) {
            var matchedString = matcher.group(2);
            filteredHighlightStringBuilder.append(highlightString, previousGroupEnd, matcher.start(1));
            filteredHighlightStringBuilder.append(matchedString);
            matchedSubstrings.add(new TaggedSubstring(
                    matcher.start(2),
                    matcher.end(2),
                    matchedString));
            previousGroupEnd = matcher.end(1);
        }
        filteredHighlightStringBuilder.append(highlightString.substring(previousGroupEnd));

        return new Highlight(filteredHighlightStringBuilder.toString(), matchedSubstrings);
    }

    public void close() throws IOException {
        client.close();
    }
}
