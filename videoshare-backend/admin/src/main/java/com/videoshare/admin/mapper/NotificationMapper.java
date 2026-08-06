package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {

    /** 删除引用某视频的通知 */
    Integer deleteByVideoId(@Param("videoId") String videoId);
}
