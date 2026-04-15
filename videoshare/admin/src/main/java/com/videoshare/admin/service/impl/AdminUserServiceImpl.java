// 路径: admin/src/main/java/com/videoshare/admin/service/impl/AdminUserServiceImpl.java
package com.videoshare.admin.service.impl;

import com.videoshare.admin.mapper.AdminUserMapper;
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

    // 
    //  分页查询用户列表
    // 
    @Override
    public PaginationResultVO<UserInfoVO> getUserList(UserInfoQuery query) {

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

        // ① 按状态统计用户数
        // 返回：[{ status: 1, count: 95 }, { status: 0, count: 5 }]
        List<Map<String, Object>> statusCounts = adminUserMapper.countByStatus();

        int totalUsers    = 0;
        int activeUsers   = 0;
        int disabledUsers = 0;

        for (Map<String, Object> row : statusCounts) {
            int s = ((Number) row.get("status")).intValue();
            int c = ((Number) row.get("count")).intValue();
            totalUsers += c;
            if (s == 1) activeUsers   = c;
            if (s == 0) disabledUsers = c;
        }

        stats.put("totalUsers",    totalUsers);
        stats.put("activeUsers",   activeUsers);
        stats.put("disabledUsers", disabledUsers);

        // ② 最近7天每天新增用户数（用于折线图）
        List<Map<String, Object>> daily = adminUserMapper.countDailyRegister(7);
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
