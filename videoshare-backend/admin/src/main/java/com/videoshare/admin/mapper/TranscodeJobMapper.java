package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TranscodeJobMapper {

    /** 删除某视频的转码任务 */
    Integer deleteByVideoId(@Param("videoId") String videoId);
}
