package com.videoshare.web.mapper;

import com.videoshare.common.entity.PlaylistVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistVideoMapper {
    Integer insert(PlaylistVideo playlistVideo);

    Integer deleteByPlaylistIdAndVideoId(@Param("playlistId") Long playlistId,
                                         @Param("videoId") String videoId);

    Integer deleteByPlaylistId(@Param("playlistId") Long playlistId);

    List<PlaylistVideo> selectByPlaylistId(@Param("playlistId") Long playlistId,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    Integer countByPlaylistId(@Param("playlistId") Long playlistId);

    Integer selectMaxSortOrder(@Param("playlistId") Long playlistId);

    Integer updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    Integer countByPlaylistIdAndVideoId(@Param("playlistId") Long playlistId,
                                        @Param("videoId") String videoId);
}
