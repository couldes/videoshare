package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WatchHistoryMapper {

    /** 删除某视频的全部观看历史记录 */
    Integer deleteByVideoId(@Param("videoId") String videoId);
}
