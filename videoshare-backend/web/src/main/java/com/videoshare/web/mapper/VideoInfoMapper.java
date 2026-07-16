// 路径: web/src/main/java/com/videoshare/web/mapper/VideoInfoMapper.java
package com.videoshare.web.mapper;

import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.query.VideoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoInfoMapper {
    /** 插入视频 */
    Integer insert(VideoInfo videoInfo);

    /** 分页查询（支持分类/关键词/用户过滤） */
    List<VideoInfo> selectVideoList(VideoQuery query);

    /** 计数（与 selectVideoList 条件一致，用于分页） */
    Integer countVideos(VideoQuery query);

    /** 按 ID 查单个视频 */
    VideoInfo selectByVideoId(@Param("videoId") String videoId);

    /** 批量查视频（按 videoId 列表） */
    List<VideoInfo> selectByVideoIds(@Param("videoIds") List<String> videoIds);

    /** 播放量 +1（高频操作，用 UPDATE 直接写库，实际项目可用 Redis 缓存） */
    Integer increaseViewCount(@Param("videoId") String videoId);

    /** 点赞数 +count（count 可为负数，即取消点赞） */
    Integer updateLikeCount(@Param("videoId") String videoId, @Param("count") int count);

    /** 收藏数 +count */
    Integer updateFavoriteCount(@Param("videoId") String videoId, @Param("count") int count);

    /** 评论数 +count */
    Integer updateCommentCount(@Param("videoId") String videoId, @Param("count") int count);

    /** 修改状态（管理员下架/发布） */
    Integer updateStatus(@Param("videoId") String videoId, @Param("status") Integer status);

    /** 按用户ID统计视频数 */
    Long countByUserId(@Param("userId") String userId);

    /** 统计某用户所有视频的被点赞总数 */
    Long sumLikeCountByUserId(@Param("userId") String userId);

    /** 热门列表 — 按热度降序 */
    List<VideoInfo> selectTrendingList(@Param("offset") int offset, @Param("limit") int limit);

    /** 订阅列表 — 关注者视频按时间倒序 */
    List<VideoInfo> selectSubscriptionVideos(@Param("userId") String userId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    /** 订阅视频总数 */
    Integer countSubscriptionVideos(@Param("userId") String userId);

    /** 更新单条热度分 */
    Integer updateHeat(@Param("videoId") String videoId, @Param("heat") double heat);

    /** 全量重算热度（定时任务用）*/
    Integer updateAllHeat();

    /** 转码完成后更新视频信息 */
    Integer updateTranscodeResult(@Param("videoId") String videoId,
                                  @Param("videoUrl") String videoUrl,
                                  @Param("duration") Integer duration,
                                  @Param("coverUrl") String coverUrl,
                                  @Param("status") Integer status);
}