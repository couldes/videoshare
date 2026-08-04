package com.videoshare.gateway.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;

class JsonExceptionHandlerTest {

    private JsonExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JsonExceptionHandler();
    }

    @Test
    void shouldReturnJsonForException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/some-path").build());

        handler.handle(exchange, new RuntimeException("测试异常")).block();

        MockServerHttpResponse response = exchange.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        String body = response.getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("\"status\":\"error\""));
        assertTrue(body.contains("测试异常"));
    }

    @Test
    void shouldHandleNullMessageException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/some-path").build());

        handler.handle(exchange, new RuntimeException()).block();

        MockServerHttpResponse response = exchange.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        String body = response.getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("\"status\":\"error\""));
        assertTrue(body.contains("RuntimeException"));
    }

    @Test
    void shouldWrapCauseMessage() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/some-path").build());

        Exception cause = new IllegalArgumentException("底层错误");
        Exception ex = new RuntimeException(null, cause);

        handler.handle(exchange, ex).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("底层错误"));
    }

    @Test
    void shouldReturnJsonContentType() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/web/some-path").build());

        handler.handle(exchange, new Exception("test")).block();

        assertEquals(MediaType.APPLICATION_JSON,
                exchange.getResponse().getHeaders().getContentType());
    }
}
