package com.videoshare.web.mapper;

import com.videoshare.common.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistMapper {
    Integer insert(Playlist playlist);

    Integer update(Playlist playlist);

    Integer deleteByPlaylistId(@Param("playlistId") Long playlistId);

    Playlist selectByPlaylistId(@Param("playlistId") Long playlistId);

    /** 查询用户的公开播放列表；isPrivate=1 时只看公开 */
    List<Playlist> selectByUserId(@Param("userId") String userId,
                                  @Param("isPrivate") Integer isPrivate);

    Integer countVideoById(@Param("playlistId") Long playlistId);

    Integer updateCoverUrl(@Param("playlistId") Long playlistId,
                           @Param("coverUrl") String coverUrl);
}
