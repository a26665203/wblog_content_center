package com.hugo.wblog.content.configuration;

import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@Configuration
public class EslasticSearchConf {
    @Bean("elasticsearchTemplate")
    public ElasticsearchOperations elasticSearchTemplate(Client client){
        return new ElasticsearchTemplate(client);
    }

}
