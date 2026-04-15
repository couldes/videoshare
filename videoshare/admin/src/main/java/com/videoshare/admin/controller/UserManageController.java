
package com.videoshare.admin.controller;

import com.videoshare.admin.service.AdminUserService;
import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.query.UserInfoQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.exception.BusinessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


//GET    /admin/user/list              → 分页查询用户列表
//POST   /admin/user/updateStatus      → 修改用户状态（启用/禁用）
//DELETE /admin/user/delete/{userId}   → 删除用户
//GET    /admin/user/dashboard         → Dashboard 统计数据
@RestController
@RequestMapping("/admin/user")
public class UserManageController extends ABaseController {

    @Resource
    private AdminUserService adminUserService;
    //  所有参数都是可选的：
    //    - 不传 keyword → 查所有用户
    //    - 不传 status  → 不按状态筛选
    // 
    @GetMapping("/list")
    public ResponseVO<PaginationResultVO<UserInfoVO>> getUserList(UserInfoQuery query) {
        // UserInfoQuery 的字段和 URL 参数名一致，Spring 会自动绑定
        // 等效于：query.setPageNum(1); query.setKeyword("张"); ...
        try {
            PaginationResultVO<UserInfoVO> result = adminUserService.getUserList(query);
            return success(result);
        } catch (Exception e) {
            return error("查询失败：" + e.getMessage());
        }
    }

    // 
    //  修改用户状态
    //  POST /admin/user/updateStatus
    //  参数：userId, status（0=禁用，1=启用）
    // 
    @PostMapping("/updateStatus")
    public ResponseVO<Void> updateUserStatus(
            @RequestParam String  userId,
            @RequestParam Integer status) {
        try {
            adminUserService.updateUserStatus(userId, status);
            return success();
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("操作失败，请稍后再试");
        }
    }

    // 
    //  删除用户
    //  DELETE /admin/user/delete/{userId}
    //  使用 REST 风格，userId 放在路径里
    // 
    @DeleteMapping("/delete/{userId}")
    public ResponseVO<Void> deleteUser(@PathVariable String userId) {
        try {
            adminUserService.deleteUser(userId);
            return success();
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除失败，请稍后再试");
        }
    }

    // 
    //  Dashboard 统计
    //  GET /admin/user/dashboard
    //  返回：{ totalUsers, activeUsers, disabledUsers, dailyRegister:[...] }
    // 
    @GetMapping("/dashboard")
    public ResponseVO<Map<String, Object>> getDashboard() {
        try {
            Map<String, Object> stats = adminUserService.getDashboardStats();
            return success(stats);
        } catch (Exception e) {
            return error("获取统计数据失败");
        }
    }
}
