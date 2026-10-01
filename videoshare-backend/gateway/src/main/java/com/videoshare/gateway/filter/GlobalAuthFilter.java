package com.videoshare.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.videoshare.common.constants.Constants;
import com.videoshare.common.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalAuthFilter.class);

    private static final String ADMIN_TOKEN_PREFIX = "ADMIN_TOKEN_";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 防御性检查：确保 request 不为 null
        if (request == null) {
            log.error("请求对象为空，直接放行");
            return chain.filter(exchange);
        }
        
        String path = request.getURI().getPath();
        String method = request.getMethodValue();

        log.info("请求：{} {}", method, path);

        // F4: 拦截内部接口
        if (path.contains("/innerApi")) {
            return writeJsonResponse(exchange, HttpStatus.FORBIDDEN,
                    ResponseVO.error("内部接口禁止外部访问"));
        }

        // F2: 提取并校验 token（添加空值防御）
        String header = request.getHeaders().getFirst("Authorization");
        
        // 安全处理 token（防止空指针并去除空白字符）
        String token = header != null ? header.trim() : null;
        
        if (token != null && !token.isEmpty()) {
            try {
                // 先查 admin token
                String adminAccount = stringRedisTemplate.opsForValue()
                        .get(ADMIN_TOKEN_PREFIX + token);
                if (adminAccount != null) {
                    ServerHttpRequest mutated = request.mutate()
                            .header("X-Admin-Account", adminAccount)
                            .build();
                    exchange = exchange.mutate().request(mutated).build();
                    log.debug("Admin token 有效：{}", adminAccount);
                } else {
                    // 再查用户 token
                    String userId = stringRedisTemplate.opsForValue()
                            .get(Constants.TOKEN_PREFIX + token);
                    if (userId != null) {
                        ServerHttpRequest mutated = request.mutate()
                                .header("X-User-Id", userId)
                                .build();
                        exchange = exchange.mutate().request(mutated).build();
                        log.debug("User token 有效：{}", userId);
                    }
                }
            } catch (Exception e) {
                // Redis 异常记录完整信息便于诊断
                log.warn("Redis 不可用或 token 验证失败，error={}", e.getMessage(), e);
                // 允许请求继续（降级处理，不阻断用户）
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> writeJsonResponse(ServerWebExchange exchange, HttpStatus status,
                                          ResponseVO<?> body) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
