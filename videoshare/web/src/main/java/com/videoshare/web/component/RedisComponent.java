
package com.videoshare.web.component;

import com.videoshare.common.constants.Constants;
import com.videoshare.common.utils.StringTools;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisComponent {

    //  验证码相关 
    private static final String CHECK_CODE_PREFIX = Constants.CHECK_CODE_PREFIX;
    private static final long   CHECK_CODE_TTL    = Constants.CHECK_CODE_TTL; // 10分钟

    //  Token 相关
    private static final String TOKEN_PREFIX = Constants.TOKEN_PREFIX;
    private static final long   TOKEN_TTL    = Constants.TOKEN_TTL;  // 7天

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //  验证码：存 
    public String saveCheckCode(String code) {
        String key = CHECK_CODE_PREFIX + StringTools.getRandomNumber(5);
        stringRedisTemplate.opsForValue().set(key, code, CHECK_CODE_TTL, TimeUnit.MINUTES);//名字，值，时限，时限单位
        return key;
    }

    //  验证码：取 
    public String getCheckCode(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    //  验证码：删 
    public void cleanCheckCode(String key) {
        if (key != null) {
            stringRedisTemplate.delete(key);
        }
    }

    //  Token：存，登录成功后，把 token → userId 的映射存入 Redis，有效期7天
    public void saveUserToken(String token, String userId) {
        String key = TOKEN_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, userId, TOKEN_TTL, TimeUnit.DAYS);
    }

    //  Token：取,根据 token 查出对应的 userId（用于后续接口鉴权）
    public String getUserIdByToken(String token) {
        return stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
    }
}