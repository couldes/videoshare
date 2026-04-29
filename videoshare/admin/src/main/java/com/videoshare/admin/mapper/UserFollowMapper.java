package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserFollowMapper {

    /** 查询用户的关注列表 */
    List<Map<String, Object>> selectFollowing(@Param("userId") String userId,
                                              @Param("offset") Integer offset,
                                              @Param("pageSize") Integer pageSize);

    /** 查询用户的粉丝列表 */
    List<Map<String, Object>> selectFollowers(@Param("userId") String userId,
                                              @Param("offset") Integer offset,
                                              @Param("pageSize") Integer pageSize);

    /** 统计关注数 */
    Long countFollowing(@Param("userId") String userId);

    /** 统计粉丝数 */
    Long countFollowers(@Param("userId") String userId);

    /** 管理员强制解除关注关系 */
    Integer delete(@Param("userId") String userId,
                   @Param("followUserId") String followUserId);
}
