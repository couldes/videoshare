// 路径: admin/src/main/java/com/videoshare/admin/service/impl/AdminUserServiceImpl.java
package com.videoshare.admin.service.impl;

import com.videoshare.admin.mapper.AdminUserMapper;
import com.videoshare.admin.mapper.UserActionMapper;
import com.videoshare.admin.mapper.UserFollowMapper;
import com.videoshare.admin.service.AdminUserService;
import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.query.UserInfoQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private UserActionMapper userActionMapper;
    @Resource
    private UserFollowMapper userFollowMapper;

    // 
    //  分页查询用户列表
    // 
    @Override
    public PaginationResultVO<UserInfoVO> getUserList(UserInfoQuery query) {//泛型用<>收参数

        // 1. 查当前页数据（SQL: SELECT ... LIMIT ? OFFSET ?）
        List<UserInfo> userList = adminUserMapper.selectUserList(query);

        // 2. 查符合条件的总数（SQL: SELECT COUNT(1) ...）
        //    注意：计数和分页是两次独立的 SQL 查询
        Integer total = adminUserMapper.countUsers(query);

        // 3. 把 UserInfo 实体列表 → UserInfoVO 列表（去掉敏感字段，加描述字段）
        List<UserInfoVO> voList = new ArrayList<>();
        for (UserInfo user : userList) {
            voList.add(convertToVO(user));
        }

        // 4. 包装成分页结果并返回
        return new PaginationResultVO<>(total, query.getPageSize(), query.getPageNum(), voList);
    }

    // 
    //  修改用户状态
    // 
    @Override
    public void updateUserStatus(String userId, Integer status) {
        // 校验 status 值是否合法（只能是 0 或 1）
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("非法的状态值");
        }

        Integer rows = adminUserMapper.updateStatus(userId, status);
        if (rows == 0) {
            // 影响行数为0说明用户不存在
            throw new BusinessException("用户不存在");
        }
    }

    // 
    //  删除用户
    // 
    @Override
    public void deleteUser(String userId) {
        Integer rows = adminUserMapper.deleteByUserId(userId);
        if (rows == 0) {
            throw new BusinessException("用户不存在或已被删除");
        }
    }

    // 
    //  Dashboard 统计数据
    // 
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 修复：使用 UNION ALL 保证始终返回两行，避免空指针异常
        List<Map<String, Object>> statusCounts = adminUserMapper.countByStatus();

        int totalUsers    = 0;
        int activeUsers   = 0;
        int disabledUsers = 0;

        for (Map<String, Object> row : statusCounts) {
            // 修复：Object → Number 转换加 null 检查，兼容 MySQL 返回 Long/Integer
            Object statusObj = row.get("status");
            Object countObj  = row.get("count");
            if (statusObj == null || countObj == null) continue;

            int s = ((Number) statusObj).intValue();
            int c = ((Number) countObj).intValue();
            totalUsers += c;
            if (s == 1) activeUsers   = c;
            if (s == 0) disabledUsers = c;
        }

        stats.put("totalUsers",    totalUsers);
        stats.put("activeUsers",   activeUsers);
        stats.put("disabledUsers", disabledUsers);

        // ② 最近7天注册折线数据
        List<Map<String, Object>> daily = adminUserMapper.countDailyRegister(7);
        // 修复：确保 count 字段统一为 Integer，防止前端图表解析失败
        for (Map<String, Object> row : daily) {
            Object c = row.get("count");
            if (c != null) row.put("count", ((Number) c).intValue());
        }
        stats.put("dailyRegister", daily);

        return stats;
    }

    // 
    //  私有辅助方法：UserInfo → UserInfoVO
    // 
    private UserInfoVO convertToVO(UserInfo user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setNickName(user.getNickName());
        vo.setEmail(user.getEmail());
        vo.setSex(user.getSex());
        vo.setStatus(user.getStatus());
        vo.setJoinTime(user.getJoinTime());

        // 把数字转成描述文字，方便前端显示（也可以在前端做，这里演示后端处理）
        vo.setSexDesc(getSexDesc(user.getSex()));
        vo.setStatusDesc(user.getStatus() == 1 ? "启用" : "禁用");

        return vo;
    }

    @Override
    public Map<String, Object> getUserActions(String userId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        Map<String, Object> result = new HashMap<>();
        result.put("likes",     userActionMapper.selectByUserId(userId, 1, offset, pageSize));
        result.put("favorites", userActionMapper.selectByUserId(userId, 2, offset, pageSize));
        return result;
    }

    @Override
    public Map<String, Object> getUserFollows(String userId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        Map<String, Object> result = new HashMap<>();
        result.put("following", userFollowMapper.selectFollowing(userId, offset, pageSize));
        result.put("followers", userFollowMapper.selectFollowers(userId, offset, pageSize));
        result.put("followingCount", userFollowMapper.countFollowing(userId));
        result.put("followerCount",  userFollowMapper.countFollowers(userId));
        return result;
    }

    private String getSexDesc(Integer sex) {
        if (sex == null) {
            return "保密";
        }

        switch (sex) {
            case 0:
                return "男";
            case 1:
                return "女";
            default:
                return "保密";
        }
    }
}
