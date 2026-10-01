package com.videoshare.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.videoshare.common.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 上传路径鉴权：检查 X-User-Id（由 GlobalAuthFilter 在用户 token 有效时注入）
 */
@Component
public class UserAuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<UserAuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(UserAuthGatewayFilterFactory.class);

    public UserAuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // 防御性检查：获取用户 ID（添加空值判断和日志增强）
            String userId = exchange.getRequest().getHeaders()
                    .getFirst("X-User-Id");

            if (userId == null || userId.trim().isEmpty()) {
                log.warn("用户路径未登录访问：{}, headerValue={}", path, userId);
                return writeJsonResponse(exchange,
                        ResponseVO.error("请先登录或登录已过期"));
            }

            return chain.filter(exchange);
        };
    }

    @Override
    public String name() {
        return "UserAuth";
    }

    private Mono<Void> writeJsonResponse(ServerWebExchange exchange, ResponseVO<?> body) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
