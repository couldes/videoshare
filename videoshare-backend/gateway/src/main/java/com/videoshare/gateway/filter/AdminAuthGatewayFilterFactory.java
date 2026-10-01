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
import java.util.Arrays;
import java.util.List;

@Component
public class AdminAuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AdminAuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthGatewayFilterFactory.class);

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/admin/account/login",
            "/admin/account/checkCode"
    );

    public AdminAuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // F3: 排除 login/checkCode
            if (EXCLUDE_PATHS.contains(path)) {
                return chain.filter(exchange);
            }

            // 检查请求头中的 admin 标识（空值防御 + 日志增强）
            String adminAccount = exchange.getRequest().getHeaders()
                    .getFirst("X-Admin-Account");

            // 防御性检查：防止空指针并记录调试信息
            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                log.warn("Admin 路径未登录访问：{}, headerValue={}", path, adminAccount);
                return writeJsonResponse(exchange,
                        ResponseVO.error("未登录或登录已过期，请重新登录"));
            }

            return chain.filter(exchange);
        };
    }

    @Override
    public String name() {
        return "AdminAuth";
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
