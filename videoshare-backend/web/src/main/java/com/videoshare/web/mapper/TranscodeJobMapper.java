package com.videoshare.web.mapper;

import com.videoshare.common.entity.TranscodeJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TranscodeJobMapper {
    Integer insert(TranscodeJob job);

    Integer updateStatus(@Param("jobId") Long jobId,
                         @Param("status") int status,
                         @Param("errorMsg") String errorMsg);

    TranscodeJob selectByVideoId(@Param("videoId") String videoId);
}
