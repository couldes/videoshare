// 路径: web/src/main/java/com/videoshare/web/mapper/UserActionMapper.java
package com.videoshare.web.mapper;

import com.videoshare.common.entity.VideoInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserActionMapper {

    /**
     * 新增行为记录（点赞/收藏）
     * 使用 INSERT IGNORE 防止重复插入（表上有 unique key）
     */
    Integer insert(@Param("userId") String userId,
                   @Param("targetId") String targetId,
                   @Param("actionType") Integer actionType);

    /**
     * 删除行为记录（取消点赞/收藏）
     */
    Integer delete(@Param("userId") String userId,
                   @Param("targetId") String targetId,
                   @Param("actionType") Integer actionType);

    /**
     * 检查是否存在指定行为（1=存在，0=不存在）
     */
    Integer checkAction(@Param("userId") String userId,
                        @Param("targetId") String targetId,
                        @Param("actionType") Integer actionType);

    /**
     * 查询用户收藏的视频ID列表（用于收藏页分页）
     */
    List<String> selectFavoriteVideoIds(@Param("userId") String userId,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);

    /**
     * 查询用户所有交互视频ID（点赞和收藏，用于个性化推荐）
     */
    List<String> selectInteractedVideoIds(@Param("userId") String userId);

    /**
     * 查询所有用户的点赞/收藏行为（用于协同过滤计算）
     */
    List<Map<String, Object>> selectAllInteractions();

    /**
     * 统计用户收藏总数
     */
    Integer countFavorites(@Param("userId") String userId);
}