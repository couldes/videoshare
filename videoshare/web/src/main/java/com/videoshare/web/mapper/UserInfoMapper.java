// 路径: web/src/main/java/com/videoshare/web/mapper/UserInfoMapper.java
package com.videoshare.web.mapper;

import com.videoshare.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据库操作接口（MyBatis 会自动生成实现）
 * 你只需要定义方法，SQL 写在 XML 文件里
 */
@Mapper  // 告诉 Spring：这是一个 MyBatis Mapper，请自动管理它
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