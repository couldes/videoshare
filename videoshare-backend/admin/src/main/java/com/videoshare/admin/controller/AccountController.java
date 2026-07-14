
package com.videoshare.admin.controller;

import com.videoshare.admin.component.AdminRedisComponent;
import com.videoshare.admin.config.AdminProperties;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.utils.StringTools;
import com.wf.captcha.SpecCaptcha;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 *   管理员账号接口
 *   接口列表：
 *   GET  /admin/account/checkCode  → 获取验证码
 *   POST /admin/account/login      → 管理员登录
 *   POST /admin/account/logout     → 退出登录（需要 token）
 */
@RestController
@RequestMapping("/admin/account")
public class AccountController extends ABaseController {

    private static final int CAPTCHA_WIDTH  = 130;
    private static final int CAPTCHA_HEIGHT = 48;

    @Resource
    private AdminProperties adminProperties;

    @Resource
    private AdminRedisComponent adminRedisComponent;


    //  生成验证码，将图片发往前端，将文字储存redis（与 web 模块逻辑完全相同）
    @GetMapping("/checkCode")
    public ResponseVO<Map<String, String>> getCheckCode() {
        try {
            SpecCaptcha captcha = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, 4);
            String checkCodeKey = adminRedisComponent.saveCheckCode(captcha.text());//保存base64码到redis（开门钥匙）

            Map<String, String> result = new HashMap<>();
            result.put("checkCode",captcha.toBase64());
            result.put("checkCodeKey",checkCodeKey);
            return success(result);//将图片发往前端
        } catch (Exception e) {
            return error("验证码生成失败，请稍后再试");
        }
    }


    //  管理员登录
    //  参数：account, password, checkCode, checkCodeKey
    @PostMapping("/login")
    public ResponseVO<?> login(
            @RequestParam String account,
            @RequestParam String password,
            @RequestParam String checkCode,
            @RequestParam String checkCodeKey) {

        try {
            // 验证码校验
            String savedCode = adminRedisComponent.getCheckCode(checkCodeKey);
            if (savedCode == null || !savedCode.equalsIgnoreCase(checkCode)) {
                throw new BusinessException("验证码错误或已过期");
            }

            // 账号校验（和 yml 配置比较）
            if (!adminProperties.getAccount().equals(account)) {
                throw new BusinessException("账号或密码错误");
            }

            String encodedPwd = StringTools.encodeByMd5(password);
            String encodedconfigPwd  = StringTools.encodeByMd5(adminProperties.getPassword());
            if (!encodedPwd.equals(encodedconfigPwd)) {
                throw new BusinessException("账号或密码错误");
            }

            // 生成 admin token 并存入 Redis
            String token = adminRedisComponent.saveAdminToken(account);

            // 返回 token 给前端
            Map<String, String> data = new HashMap<>();
            data.put("token",   token);
            data.put("account", account);
            return success(data);

        } catch (BusinessException e) {
            return error(e.getMessage());
        } finally {
            // 验证码无论成败都销毁（只能用一次）
            adminRedisComponent.cleanCheckCode(checkCodeKey);
        }
    }


    // 
    //  退出登录（需要 token，被拦截器保护）
    //  POST /admin/account/logout
    // 
    @PostMapping("/logout")
    public ResponseVO<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        adminRedisComponent.removeAdminToken(token);
        return success();
    }
}
