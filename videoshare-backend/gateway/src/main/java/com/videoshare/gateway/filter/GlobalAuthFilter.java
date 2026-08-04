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
        String path = request.getURI().getPath();
        String method = request.getMethodValue();

        log.info("请求: {} {}", method, path);

        // F4: 拦截内部接口
        if (path.contains("/innerApi")) {
            return writeJsonResponse(exchange, HttpStatus.FORBIDDEN,
                    ResponseVO.error("内部接口禁止外部访问"));
        }

        // F2: 提取并校验 token
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && !token.trim().isEmpty()) {
            try {
                // 先查 admin token
                String adminAccount = stringRedisTemplate.opsForValue()
                        .get(ADMIN_TOKEN_PREFIX + token);
                if (adminAccount != null) {
                    ServerHttpRequest mutated = request.mutate()
                            .header("X-Admin-Account", adminAccount)
                            .build();
                    exchange = exchange.mutate().request(mutated).build();
                    log.debug("Admin token 有效: {}", adminAccount);
                } else {
                    // 再查用户 token
                    String userId = stringRedisTemplate.opsForValue()
                            .get(Constants.TOKEN_PREFIX + token);
                    if (userId != null) {
                        ServerHttpRequest mutated = request.mutate()
                                .header("X-User-Id", userId)
                                .build();
                        exchange = exchange.mutate().request(mutated).build();
                        log.debug("User token 有效: {}", userId);
                    }
                }
            } catch (Exception e) {
                log.warn("Redis 不可用，跳过 Gateway 层 token 校验: {}", e.getMessage());
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
