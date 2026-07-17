// 路径: web/src/main/java/com/videoshare/web/mapper/UserFollowMapper.java
package com.videoshare.web.mapper;

import com.videoshare.common.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowMapper {
    /** 建立关注关系 */
    Integer insert(@Param("userId") String userId, @Param("followUserId") String followUserId);

    /** 取消关注 */
    Integer delete(@Param("userId") String userId, @Param("followUserId") String followUserId);

    /** 是否已关注（返回1=是，0=否）*/
    Integer checkFollow(@Param("userId") String userId, @Param("followUserId") String followUserId);

    /** 粉丝数（被关注数）*/
    Long countFollowers(@Param("userId") String userId);

    /** 关注数 */
    Long countFollowing(@Param("userId") String userId);

    /** 查询关注列表 */
    List<UserInfo> selectFollowingList(@Param("userId") String userId);

    /** 查询粉丝列表 */
    List<UserInfo> selectFollowerList(@Param("followUserId") String userId);
}