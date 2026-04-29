
package com.videoshare.web.mapper;

import com.videoshare.common.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//数据库操作接口

@Mapper
public interface UserInfoMapper {

    // 根据邮箱查询用户（注册时判断邮箱是否已存在）
    UserInfo selectByEmail(@Param("email") String email);

    // 根据昵称查询用户（注册时判断昵称是否已存在）
    UserInfo selectByNickName(@Param("nickName") String nickName);

    // 插入新用户
    Integer insert(UserInfo userInfo);

    // 根据昵称删除用户
    Integer deleteByNickName(@Param("nickName") String nickName);

    //按用户ID查询
    UserInfo selectByUserId(@Param("userId") String userId);

    // 更新个人简介和头像
    Integer updateProfile(@Param("userId")    String userId,
                          @Param("bio")       String bio,
                          @Param("avatarUrl") String avatarUrl);

    // 搜索用户（按昵称/邮箱模糊匹配）
    List<UserInfo> searchByKeyword(@Param("keyword") String keyword);
}