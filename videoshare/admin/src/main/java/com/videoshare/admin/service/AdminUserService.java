// 路径: admin/src/main/java/com/videoshare/admin/service/AdminUserService.java
package com.videoshare.admin.service;

import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.query.UserInfoQuery;
import com.videoshare.common.vo.PaginationResultVO;

import java.util.Map;


public interface AdminUserService {

    /** 分页查询用户列表 */
    PaginationResultVO<UserInfoVO> getUserList(UserInfoQuery query);

    /** 修改用户状态（启用/禁用） */
    void updateUserStatus(String userId, Integer status);

    /** 删除用户 */
    void deleteUser(String userId);

    /** Dashboard 统计数据 */
    Map<String, Object> getDashboardStats();
}
