package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaylistVideoMapper {

    /** 删除播放列表中该视频的条目 */
    Integer deleteByVideoId(@Param("videoId") String videoId);
}
