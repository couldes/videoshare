package com.videoshare.web.mapper;

import com.videoshare.common.entity.WatchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WatchHistoryMapper {

    Integer upsert(@Param("userId") String userId, @Param("videoId") String videoId);

    Integer deleteByUserAndVideo(@Param("userId") String userId, @Param("videoId") String videoId);

    Integer deleteByUser(@Param("userId") String userId);

    List<WatchHistory> selectPage(@Param("userId") String userId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    Integer countByUser(@Param("userId") String userId);

    List<WatchHistory> searchByKeyword(@Param("userId") String userId,
                                       @Param("keyword") String keyword,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    Integer countSearch(@Param("userId") String userId,
                        @Param("keyword") String keyword);

    Integer cleanupExcess(@Param("userId") String userId,
                          @Param("keepCount") int keepCount);
}
