
package com.videoshare.web.mapper;

import com.videoshare.common.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}