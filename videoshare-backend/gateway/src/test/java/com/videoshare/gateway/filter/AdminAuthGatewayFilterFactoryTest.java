package com.videoshare.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminAuthGatewayFilterFactoryTest {

    private AdminAuthGatewayFilterFactory factory;
    private GatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        factory = new AdminAuthGatewayFilterFactory();
        filter = factory.apply(new AdminAuthGatewayFilterFactory.Config());
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldExcludeLoginPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin/account/login").build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void shouldExcludeCheckCodePath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin/account/checkCode").build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void shouldBlockRequestWithoutAdminHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin/admin/video/audit").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldBlockRequestWithEmptyAdminHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin/admin/video/audit")
                        .header("X-Admin-Account", "")
                        .build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldPassRequestWithValidAdminHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin/admin/video/audit")
                        .header("X-Admin-Account", "admin")
                        .build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void shouldReturnFilterName() {
        assertEquals("AdminAuth", factory.name());
    }
}
