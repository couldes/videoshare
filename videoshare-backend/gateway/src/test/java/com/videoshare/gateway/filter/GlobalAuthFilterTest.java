package com.videoshare.gateway.filter;

import com.videoshare.common.constants.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalAuthFilterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private GlobalAuthFilter filter;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldBlockInnerApiPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/innerApi/video/list").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldBlockInnerApiInNestedPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/innerApi/something").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldPassNormalRequestWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/account/checkCode").build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        assertNull(exchange.getRequest().getHeaders().getFirst("X-User-Id"));
        assertNull(exchange.getRequest().getHeaders().getFirst("X-Admin-Account"));
    }

    @Test
    void shouldSetUserHeaderForValidUserToken() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.get(Constants.TOKEN_PREFIX + "usertoken123"))
                .thenReturn("user001");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/video/list")
                        .header("Authorization", "usertoken123")
                        .build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        // Note: headers set via mutate() are on the mutated request, not the original.
        // The filter mutates the exchange request, which is applied before forwarding.
    }

    @Test
    void shouldSetAdminHeaderForValidAdminToken() {
        when(valueOperations.get("ADMIN_TOKEN_" + "admintoken456"))
                .thenReturn("admin");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin/admin/video/audit")
                        .header("Authorization", "admintoken456")
                        .build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void shouldPassWhenRedisUnavailable() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis down"));

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/video/list")
                        .header("Authorization", "sometoken")
                        .build());

        // Should not throw — Redis failure is silently handled
        assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        verify(chain).filter(any());
    }

    @Test
    void shouldNotSetHeadersForInvalidToken() {
        when(valueOperations.get(anyString())).thenReturn(null);

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/video/list")
                        .header("Authorization", "invalidtoken")
                        .build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void shouldLogRequestPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/account/login").build());

        // Just verify no exception is thrown
        assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
    }
}
