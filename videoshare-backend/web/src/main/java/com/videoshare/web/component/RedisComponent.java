
package com.videoshare.web.component;

import com.videoshare.common.constants.Constants;
import com.videoshare.common.utils.StringTools;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisComponent {

    //  验证码相关 
    private static final String CHECK_CODE_PREFIX = Constants.CHECK_CODE_PREFIX;
    private static final long   CHECK_CODE_TTL    = Constants.CHECK_CODE_TTL; // 10分钟

    //  Token 相关
    private static final String TOKEN_PREFIX = Constants.TOKEN_PREFIX;
    private static final long   TOKEN_TTL    = Constants.TOKEN_TTL;  // 7天

    //  推荐缓存相关
    private static final String REC_SIM_PREFIX = "rec:sim:";
    private static final long   REC_SIM_TTL    = 1; // 小时

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

    //  推荐缓存：存储视频相似度 ZSet
    public void saveVideoSimilarities(String videoId, Map<String, Double> similarities) {
        String key = REC_SIM_PREFIX + videoId;
        StringRedisTemplate ops = stringRedisTemplate;
        similarities.forEach((otherId, score) ->
                ops.opsForZSet().add(key, otherId, score));
        ops.expire(key, REC_SIM_TTL, TimeUnit.HOURS);
    }

    //  推荐缓存：读取视频相似度列表（带分数）
    public Set<ZSetOperations.TypedTuple<String>> getVideoSimilarities(String videoId) {
        String key = REC_SIM_PREFIX + videoId;
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
    }

    // 分布式锁：设置键如果不存在，返回设置的 TTL（毫秒），失败返回 null
    public Long setIfAbsent(String key, String value, long timeoutMs) {
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, timeoutMs, TimeUnit.MILLISECONDS);
        if (success != null && success) {
            return timeoutMs;
        }
        return null;
    }

    // 删除键（用于释放锁）
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

}