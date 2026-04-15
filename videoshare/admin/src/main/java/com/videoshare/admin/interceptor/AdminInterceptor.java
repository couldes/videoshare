// 路径: admin/src/main/java/com/videoshare/admin/interceptor/AdminInterceptor.java
package com.videoshare.admin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoshare.admin.component.AdminRedisComponent;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin 请求拦截器
 * 工作流程：
 *   前端请求 → 拦截器检查 token → 合法：放行到 Controller
 *                                → 非法：直接返回 401，不到达 Controller
 * 注意：登录接口 /admin/account/login 不需要验证 token（放行白名单）
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Resource
    private AdminRedisComponent adminRedisComponent;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * preHandle：在 Controller 方法执行【之前】调用
     * 返回 true  = 放行（继续执行 Controller）
     * 返回 false = 拦截（直接结束请求）
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {

        // 从请求头取 token（前端登录后每次请求都带上）
        // request.getHeader("Authorization") 读取 Header 里的 Authorization 字段
        String token = request.getHeader("Authorization");

        // 去 Redis 验证 token 是否有效
        String adminAccount = adminRedisComponent.getAdminByToken(token);

        if (adminAccount == null) {
            // token 无效或已过期：返回 401 状态码 + JSON 错误信息
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);       // HTTP 401
            response.setContentType("application/json;charset=UTF-8");

            // 构建错误响应：{ "status": "error", "info": "请先登录" }
            ResponseVO<Void> vo = ResponseVO.error("未登录或登录已过期，请重新登录");
            response.getWriter().write(objectMapper.writeValueAsString(vo));
            return false; // 拦截，不继续执行
        }

        // token 有效：续期（滑动过期，每次请求都延长有效时间）
        adminRedisComponent.renewAdminToken(token);

        // 把管理员账号存入 request attribute，Controller 里可以取到
        // 例：String account = (String) request.getAttribute("adminAccount");
        request.setAttribute("adminAccount", adminAccount);

        return true; // 放行
    }
}
