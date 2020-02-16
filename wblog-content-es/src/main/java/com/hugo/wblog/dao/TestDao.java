package com.hugo.wblog.dao;

import com.hugo.wblog.domain.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TestDao extends ElasticsearchRepository<Article,String> {
}
