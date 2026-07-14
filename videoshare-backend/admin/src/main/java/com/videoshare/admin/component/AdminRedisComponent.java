
package com.videoshare.admin.component;

import com.videoshare.admin.config.AdminProperties;
import com.videoshare.common.constants.Constants;
import com.videoshare.common.utils.StringTools;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Component
public class AdminRedisComponent {

    // admin token 的 Redis Key 前缀，与 user token 区分开
    private static final String ADMIN_TOKEN_PREFIX = "ADMIN_TOKEN_";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AdminProperties adminProperties; // 注入配置，获取 tokenTtl


    //  验证码相关
    private static final String CHECK_CODE_PREFIX = Constants.CHECK_CODE_PREFIX;
    private static final long   CHECK_CODE_TTL    = Constants.CHECK_CODE_TTL; // 10分钟


//登录成功后，生成并保存 admin token

    public String saveAdminToken(String adminAccount) {
        // 生成一个随机 Token（16位随机数字）
        String token = StringTools.getRandomNumber(16);
        String key   = ADMIN_TOKEN_PREFIX + token;

        stringRedisTemplate.opsForValue().set(
            key,
            adminAccount,                   // Value = 管理员账号
            adminProperties.getTokenTtl(),  // 过期时间（天数，从 yml 读取）
            TimeUnit.DAYS
        );
        return token;
    }


//根据 token 验证是否合法

    public String getAdminByToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        return stringRedisTemplate.opsForValue().get(ADMIN_TOKEN_PREFIX + token);
    }


//注销：删除 token（退出登录时调用）

    public void removeAdminToken(String token) {
        if (token != null) {
            stringRedisTemplate.delete(ADMIN_TOKEN_PREFIX + token);
        }
    }


//续期：每次有效请求重置过期时间（"滑动过期"策略）
//只要管理员一直在操作，就不会被踢出

    public void renewAdminToken(String token) {
        String key = ADMIN_TOKEN_PREFIX + token;
        stringRedisTemplate.expire(key, adminProperties.getTokenTtl(), TimeUnit.DAYS);
    }


    //  验证码：删
    public void cleanCheckCode(String key) {
        if (key != null) {
            stringRedisTemplate.delete(key);
        }
    }

    //  验证码：存
    public String saveCheckCode(String code) {
        String key = CHECK_CODE_PREFIX + StringTools.getRandomNumber(5);
        stringRedisTemplate.opsForValue().set(key, code, CHECK_CODE_TTL, TimeUnit.MINUTES);
        return key;
    }


    //  验证码：取
    public String getCheckCode(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }


}
