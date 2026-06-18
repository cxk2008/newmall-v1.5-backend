package com.itye.mall.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
@ConditionalOnProperty(prefix = "mall.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient(ElasticsearchProperties properties) {
        HttpHost httpHost = new HttpHost(
                properties.normalizedHost(),
                properties.normalizedPort(),
                properties.normalizedScheme()
        );

        return RestClient.builder(httpHost)
                .setRequestConfigCallback(requestConfig -> requestConfig
                        .setConnectTimeout(properties.normalizedConnectTimeoutMillis())
                        .setSocketTimeout(properties.normalizedSocketTimeoutMillis())
                )
                .setHttpClientConfigCallback(httpClient -> {
                    if (!properties.hasCredentials()) {
                        return httpClient;
                    }
                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(properties.username(), properties.password())
                    );
                    return httpClient.setDefaultCredentialsProvider(credentialsProvider);
                })
                .build();
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(RestClient elasticsearchRestClient, ObjectMapper objectMapper) {
        return new RestClientTransport(elasticsearchRestClient, new JacksonJsonpMapper(objectMapper));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport elasticsearchTransport) {
        return new ElasticsearchClient(elasticsearchTransport);
    }
}
