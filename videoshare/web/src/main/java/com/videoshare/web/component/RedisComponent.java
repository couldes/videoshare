// 路径: web/src/main/java/com/videoshare/web/component/RedisComponent.java
package com.videoshare.web.component;

import com.videoshare.utils.StringTools;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisComponent {

    // ===== 验证码相关 =====
    private static final String CHECK_CODE_PREFIX = "CHECK_CODE_";
    private static final long   CHECK_CODE_TTL    = 10L; // 10分钟

    // ===== Token 相关（新增）=====
    private static final String TOKEN_PREFIX = "TOKEN_";
    private static final long   TOKEN_TTL    = 7L;  // 7天

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ---------- 验证码：存 ----------
    public String saveCheckCode(String code) {
        String key = CHECK_CODE_PREFIX + StringTools.getRandomNumber(5);
        stringRedisTemplate.opsForValue().set(key, code, CHECK_CODE_TTL, TimeUnit.MINUTES);
        return key;
    }

    // ---------- 验证码：取 ----------
    public String getCheckCode(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ---------- 验证码：删 ----------
    public void cleanCheckCode(String key) {
        if (key != null) {
            stringRedisTemplate.delete(key);
        }
    }

    // ---------- Token：存（新增）----------
    /**
     * 登录成功后，把 token → userId 的映射存入 Redis
     * 有效期7天（7天不操作自动登出）
     *
     * @param token  登录凭证（给前端的）
     * @param userId 对应的用户ID
     */
    public void saveUserToken(String token, String userId) {
        String key = TOKEN_PREFIX + token;
        // Key = "TOKEN_xxxxx"，Value = userId，有效期7天
        stringRedisTemplate.opsForValue().set(key, userId, TOKEN_TTL, TimeUnit.DAYS);
    }

    // ---------- Token：取（新增）----------
    /**
     * 根据 token 查出对应的 userId（用于后续接口鉴权）
     *
     * @param token 前端请求头带来的 token
     * @return userId，查不到说明未登录或已过期
     */
    public String getUserIdByToken(String token) {
        return stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
    }
}