package com.videoshare.web.service;

import com.videoshare.common.vo.PlaylistVO;

import java.util.List;

public interface PlaylistService {
    PlaylistVO createPlaylist(String userId, String title, String description);

    PlaylistVO updatePlaylist(String userId, Long playlistId, String title,
                              String description, Integer isPrivate);

    void deletePlaylist(String userId, Long playlistId);

    void addVideo(String userId, Long playlistId, String videoId);

    void removeVideo(String userId, Long playlistId, String videoId);

    void reorderVideos(String userId, Long playlistId, java.util.List<String> videoIds);

    PlaylistVO getPlaylistDetail(Long playlistId, String currentUserId,
                                 Integer pageNum, Integer pageSize);

    List<PlaylistVO> getUserPlaylists(String targetUserId, String currentUserId);
}
