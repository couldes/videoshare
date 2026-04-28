// 路径: admin/src/main/java/com/videoshare/admin/mapper/AdminVideoMapper.java
package com.videoshare.admin.mapper;

import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.query.VideoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 管理端视频 Mapper
 * 与 web 端 VideoInfoMapper 职责不同：
 *   - web 端：面向用户（只查 status=1 的）
 *   - admin 端：面向管理员（可查所有状态，含下架/待审核）
 */
@Mapper
public interface AdminVideoMapper {

    /** 分页查询（支持关键词/状态/分类过滤，不限制 status=1）*/
    List<VideoInfo> selectVideoList(VideoQuery query);

    /** 计数（与 selectVideoList 条件一致）*/
    Integer countVideos(VideoQuery query);

    /** 修改状态（1=发布/上架 2=下架）*/
    Integer updateStatus(@Param("videoId") String videoId, @Param("status") Integer status);

    /** 删除视频（物理删除，谨慎使用）*/
    Integer deleteByVideoId(@Param("videoId") String videoId);

    /** Dashboard：各状态视频统计 */
    List<Map<String, Object>> countByStatus();

    /** Dashboard：近 N 天每日发布数 */
    List<Map<String, Object>> countDailyPublish(@Param("days") Integer days);
}