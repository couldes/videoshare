// 路径: web/src/main/java/com/videoshare/web/controller/AccountController.java
package com.videoshare.web.controller;

import com.videoshare.common.exception.BusinessException;
import com.videoshare.web.component.RedisComponent;
import com.videoshare.web.service.UserInfoService;
import com.videoshare.common.vo.ResponseVO;
import com.wf.captcha.SpecCaptcha;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController//所有方法返回的内容都会自动变成 JSON 给前端
@RequestMapping("/account")
public class AccountController extends ABaseController {

    private static final int CAPTCHA_WIDTH  = 130;
    private static final int CAPTCHA_HEIGHT = 48;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取验证码
     * GET /account/checkCode
     */
    @GetMapping("/checkCode")//查数据 → 用 @GetMapping
    public ResponseVO getCheckCode() {
        try {
            SpecCaptcha captcha = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, 4);
            String checkCodeKey = redisComponent.saveCheckCode(captcha.text());

            Map<String, String> result = new HashMap<>();
            result.put("checkCode",    captcha.toBase64());
            result.put("checkCodeKey", checkCodeKey);
            return getSuccessResponseVO(result);
        } catch (Exception e) {
            e.printStackTrace();
            return getFailureResponseVO("服务器忙，请稍后再试");
        }
    }

    /**
     * 注册
     * POST /account/register
     */
    @PostMapping("/register")
    public ResponseVO register(
            @RequestParam String email,
            @RequestParam String nickName,
            @RequestParam String registerPassword,
            @RequestParam String checkCodeKey,
            @RequestParam String checkCode) {
        try {
            // 参数全部透传，Controller 里零逻辑
            userInfoService.register(email, nickName, registerPassword, checkCode, checkCodeKey);
            return getSuccessResponseVO("注册成功");
        } catch (BusinessException e) {
            return getFailureResponseVO(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return getFailureResponseVO("服务器忙，请稍后再试");
        }
    }

    /**
     * 登录
     * POST /account/login
     *收参数 → 调Service → 返结果。业务判断全部在 Service
     */
    @PostMapping("/login")
    public ResponseVO login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String checkCode,
            @RequestParam String checkCodeKey) {
        try {
            // 一行调用，所有验证逻辑都在 Service 里
            ResponseVO ResponseVO = userInfoService.login(email, password, checkCode, checkCodeKey);
            return getSuccessResponseVO(ResponseVO);
        } catch (BusinessException e) {
            // Service 抛出的业务异常，直接把消息给用户看
            return getFailureResponseVO(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return getFailureResponseVO("服务器忙，请稍后再试");
        }
    }
}