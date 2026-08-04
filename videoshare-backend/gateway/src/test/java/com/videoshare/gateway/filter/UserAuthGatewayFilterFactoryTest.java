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

class UserAuthGatewayFilterFactoryTest {

    private UserAuthGatewayFilterFactory factory;
    private GatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        factory = new UserAuthGatewayFilterFactory();
        filter = factory.apply(new UserAuthGatewayFilterFactory.Config());
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldBlockRequestWithoutUserIdHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/resource/upload").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldBlockRequestWithEmptyUserIdHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/resource/upload")
                        .header("X-User-Id", "")
                        .build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldPassRequestWithValidUserIdHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/resource/upload")
                        .header("X-User-Id", "u123")
                        .build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void shouldReturnFilterName() {
        assertEquals("UserAuth", factory.name());
    }
}
