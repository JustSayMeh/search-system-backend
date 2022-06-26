package ru.scytech.documentsearchsystembackend.services;


import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import ru.scytech.documentsearchsystembackend.model.FileData;
import ru.scytech.documentsearchsystembackend.model.Highlight;
import ru.scytech.documentsearchsystembackend.model.SearchResult;
import ru.scytech.documentsearchsystembackend.model.TaggedSubstring;
import ru.scytech.documentsearchsystembackend.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ElasticRestClient implements Closeable {
    private RestHighLevelClient client;
    private HighlightBuilder highlightBuilder;
    private String contentFieldName;
    private String fileNameFieldName;
    private String fsIndexName;
    private String pipelineName;
    private final String HIGHLIGHTER_TYPE = "fvh";
    private boolean isInit = false;

    public ElasticRestClient(String host, int port) {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(host, port)
        ));
    }

    public void initConnection(byte[] pipelineBody,
                               byte[] indexBody,
                               String fsIndexName,
                               String pipelineName,
                               String fileNameFieldName,
                               String contentFieldName) throws IOException {
        GetPipelineRequest getPipelineRequest = new GetPipelineRequest(pipelineName);
        GetPipelineResponse getPipelineResponse = client.ingest().getPipeline(getPipelineRequest, RequestOptions.DEFAULT);
        var pipelines = getPipelineResponse.pipelines();
        if (pipelines.size() == 0) {
            PutPipelineRequest putPipelineRequest = new PutPipelineRequest(pipelineName, new BytesArray(pipelineBody), XContentType.JSON);
            AcknowledgedResponse acknowledgedResponse = client.ingest().putPipeline(putPipelineRequest, RequestOptions.DEFAULT);
            if (!acknowledgedResponse.isAcknowledged()) {
                throw new RuntimeException("Не удалось создать pipeline");
            }
        }
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(fsIndexName);
            client.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException e) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(fsIndexName);
            createIndexRequest.source(new BytesArray(indexBody), XContentType.JSON);
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged()) {
                throw new RuntimeException("Не удалось создать index");
            }
        }
        this.highlightBuilder = new HighlightBuilder().field(contentFieldName).highlighterType(HIGHLIGHTER_TYPE);
        this.fileNameFieldName = fileNameFieldName;
        this.contentFieldName = contentFieldName;
        this.pipelineName = pipelineName;
        this.fsIndexName = fsIndexName;
        this.isInit = true;
    }

    public List<SearchResult> searchPhrase(String phrase) throws IOException {
        throwExceptionIfNotInit();
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(fsIndexName);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().query(
                        QueryBuilders
                                .matchPhraseQuery(contentFieldName, phrase)
                                .slop(2))
                        .highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<SearchResult> searchResults = new ArrayList<>();
        if (response != null) {
            for (var hit : response.getHits()) {
                var fileName = hit.getSourceAsMap().get(fileNameFieldName).toString();
                List<Highlight> highLightList = new ArrayList<>();
                var highLightFields = hit.getHighlightFields();
                if (!highLightFields.containsKey(contentFieldName)) {
                    continue;
                }
                for (var text : highLightFields.get(contentFieldName).getFragments()) {
                    highLightList.add(parseHighLight(text.string(), "em"));
                }
                searchResults.add(Utils.createSearchResultFromRaw(fileName, highLightList, hit.getScore()));
            }
        }
        return searchResults;
    }

    public List<IndexResponse> indexTextFiles(List<FileData> contents) throws IOException {
        throwExceptionIfNotInit();
        List<IndexResponse> responses = new ArrayList<>();
        for (FileData fileData : contents) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(fsIndexName);
            indexRequest.id(UUID.randomUUID().toString());
            indexRequest.setPipeline(pipelineName);
            indexRequest.source(
                    fileNameFieldName,
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

    private void throwExceptionIfNotInit() {
        if (!isInit) {
            throw new RuntimeException("Connection not init! Call initConnection for init.");
        }
    }
}
