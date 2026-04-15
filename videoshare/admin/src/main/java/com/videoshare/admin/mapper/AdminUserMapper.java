// 路径: admin/src/main/java/com/videoshare/admin/mapper/AdminUserMapper.java
package com.videoshare.admin.mapper;

import com.videoshare.common.query.UserInfoQuery;
import com.videoshare.common.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理端用户数据库操作接口
 *
 * 与 web 模块的 UserInfoMapper 分开，职责不同：
 *   UserInfoMapper（web）：面向用户自身操作（注册、登录）
 *   AdminUserMapper（admin）：面向管理员的批量操作（列表、统计、状态管理）
 *
 * 两个 Mapper 操作的是同一张 user_info 表，但查询维度不同。
 * SQL 写在对应的 XML 文件里（AdminUserMapper.xml）
 */
@Mapper
public interface AdminUserMapper {

    /**
     * 分页查询用户列表（支持关键词搜索 + 状态筛选）
     * 对应 SQL：SELECT * FROM user_info WHERE ... LIMIT ? OFFSET ?
     *
     * @param query 查询条件（pageNum, pageSize, keyword, status）
     * @return 当前页的用户列表
     */
    List<UserInfo> selectUserList(UserInfoQuery query);

    /**
     * 统计符合条件的用户总数（和 selectUserList 条件一致，用于分页）
     *
     * @param query 查询条件（只用 keyword 和 status，不用分页参数）
     * @return 总记录数
     */
    Integer countUsers(UserInfoQuery query);

    /**
     * 修改用户状态（启用/禁用）
     *
     * @param userId 目标用户 ID
     * @param status 新状态（0=禁用，1=启用）
     * @return 影响行数（1=成功）
     */
    Integer updateStatus(@Param("userId") String userId, @Param("status") Integer status);

    /**
     * 删除用户（物理删除，实际项目建议改为逻辑删除）
     *
     * @param userId 目标用户 ID
     * @return 影响行数
     */
    Integer deleteByUserId(@Param("userId") String userId);

    /**
     * Dashboard 统计：各状态用户数量
     * 返回 [{ status: 0, count: 5 }, { status: 1, count: 95 }]
     */
    List<java.util.Map<String, Object>> countByStatus();

    /**
     * Dashboard 统计：最近 N 天每天的新注册用户数
     * @param days 统计天数（如 7 = 最近7天）
     */
    List<java.util.Map<String, Object>> countDailyRegister(@Param("days") Integer days);
}
