package com.videoshare.common.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoSearchServiceImpl implements VideoSearchService {

    private static final Logger log = LoggerFactory.getLogger(VideoSearchServiceImpl.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private VideoSearchRepository videoSearchRepository;

    @Override
    public SearchResult searchIds(String keyword, String orderBy, Integer pageNum, Integer pageSize) {
        try {
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

            if (keyword != null && !keyword.trim().isEmpty()) {
                // title 权重 2x，description 权重 1x
                builder.withQuery(QueryBuilders.multiMatchQuery(keyword.trim(), "title^2", "description"));
            } else {
                builder.withQuery(QueryBuilders.matchAllQuery());
            }

            // 只查已发布
            builder.withFilter(QueryBuilders.termQuery("status", 1));

            // 排序
            if ("view_count".equals(orderBy)) {
                builder.withSort(SortBuilders.fieldSort("viewCount").order(SortOrder.DESC));
            } else {
                builder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
            }

            // 分页
            int offset = (pageNum - 1) * pageSize;
            builder.withPageable(PageRequest.of(offset, pageSize));

            NativeSearchQuery query = builder.build();
            SearchHits<VideoSearchDocument> searchHits = elasticsearchOperations.search(query, VideoSearchDocument.class);

            List<String> videoIds = searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getVideoId())
                    .collect(Collectors.toList());

            return new SearchResult(videoIds, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("ES 搜索异常，keyword={}", keyword, e);
            return new SearchResult(Collections.emptyList(), 0);
        }
    }

    @Override
    public void save(VideoSearchDocument doc) {
        try {
            elasticsearchOperations.save(doc);
        } catch (Exception e) {
            log.error("ES 保存失败，videoId={}", doc.getVideoId(), e);
        }
    }

    @Override
    public void deleteById(String videoId) {
        try {
            elasticsearchOperations.delete(videoId, VideoSearchDocument.class);
        } catch (Exception e) {
            log.error("ES 删除失败，videoId={}", videoId, e);
        }
    }

    @Override
    public void updateStatus(String videoId, Integer status) {
        try {
            VideoSearchDocument doc = elasticsearchOperations.get(videoId, VideoSearchDocument.class);
            if (doc != null) {
                doc.setStatus(status);
                elasticsearchOperations.save(doc);
            }
        } catch (Exception e) {
            log.error("ES 更新状态失败，videoId={}, status={}", videoId, status, e);
        }
    }

    @Override
    public void saveAll(List<VideoSearchDocument> docs) {
        try {
            elasticsearchOperations.save(docs);
        } catch (Exception e) {
            log.error("ES 批量保存失败，数量={}", docs.size(), e);
        }
    }
}
