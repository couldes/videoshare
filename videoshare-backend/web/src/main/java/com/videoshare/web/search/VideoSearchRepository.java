package com.videoshare.web.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoSearchRepository extends ElasticsearchRepository<VideoSearchDocument, String> {
}
