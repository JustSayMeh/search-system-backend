package ru.scytech.documentsearchsystembackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import ru.scytech.documentsearchsystembackend.services.ElasticRestClient;

import java.io.IOException;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class SearchSystemBackendApplication {


    public static void main(String[] args) {
        SpringApplication.run(SearchSystemBackendApplication.class, args);
    }

    @Bean
    public ElasticRestClient getElasticRestClient(@Value("${application.elastic.server.host}") String host,
                                                  @Value("${application.elastic.server.port}") int port,
                                                  @Value("${application.elastic.files.index}") String indexName,
                                                  @Value("${application.elastic.files.fieldname}") String fileFieldName,
                                                  @Value("${application.elastic.pipelineName}") String pipelineName,
                                                  @Value("${application.elastic.content.fieldname}") String contentFieldName,
                                                  @Value("${application.filesystem.index.path}") Resource indexBody,
                                                  @Value("${application.filesystem.pipeline.path}") Resource pipelineBody)
            throws IOException {
        ElasticRestClient elasticRestClient = new ElasticRestClient(host, port);
        byte[] indexBytes = indexBody.getInputStream().readAllBytes();
        byte[] pipelineBytes = pipelineBody.getInputStream().readAllBytes();
        elasticRestClient.initConnection(pipelineBytes, indexBytes, indexName, pipelineName, fileFieldName, contentFieldName);
        return elasticRestClient;
    }
}
