package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserActionMapper {

    /** 查询用户的操作记录（actionType: 1=点赞 2=收藏 3=点赞评论）*/
    List<Map<String, Object>> selectByUserId(@Param("userId") String userId,
                                             @Param("actionType") Integer actionType,
                                             @Param("offset") Integer offset,
                                             @Param("pageSize") Integer pageSize);

    /** 统计用户操作数量 */
    Integer countByUserId(@Param("userId") String userId,
                          @Param("actionType") Integer actionType);

    /** 删除指定操作（管理员强制取消）*/
    Integer delete(@Param("userId") String userId,
                   @Param("targetId") String targetId,
                   @Param("actionType") Integer actionType);
}
