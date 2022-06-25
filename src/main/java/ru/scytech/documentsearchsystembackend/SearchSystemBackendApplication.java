package ru.scytech.documentsearchsystembackend;

import ru.scytech.documentsearchsystembackend.services.ElasticRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class SearchSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchSystemBackendApplication.class, args);
    }

    @Bean
    public ElasticRestClient getElasticRestClient(@Value("${application.elastic.server.host}") String host,
                                                  @Value("${application.elastic.server.port}") int port) {
        return new ElasticRestClient(host, port);
    }
}
