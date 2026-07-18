package com.videoshare.common.search;

import java.util.List;

public interface VideoSearchService {

    /** 全文搜索：返回匹配的 videoId 列表 */
    SearchResult searchIds(String keyword, String orderBy, Integer pageNum, Integer pageSize);

    /** 索引/更新单个视频 */
    void save(VideoSearchDocument doc);

    /** 按 videoId 删除文档 */
    void deleteById(String videoId);

    /** 更新视频状态字段 */
    void updateStatus(String videoId, Integer status);

    /** 批量索引（全量重建用） */
    void saveAll(List<VideoSearchDocument> docs);
}
