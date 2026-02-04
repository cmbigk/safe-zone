package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain chain;
    private String jwtSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", jwtSecret);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void testConfigClass() {
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        assertNotNull(config);
    }

    @Test
    void testSkipAuthenticationForLoginEndpoint() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void testSkipAuthenticationForRegisterEndpoint() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/auth/register")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void testSkipAuthenticationForGetProducts() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/products")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void testMissingAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/api/protected")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void testInvalidAuthorizationHeaderFormat() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Invalid token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void testValidToken() {
        String token = generateToken("user@example.com", "CLIENT");
        
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void testExpiredToken() {
        String expiredToken = generateExpiredToken("user@example.com", "CLIENT");
        
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void testInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    private String generateToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(key)
                .compact();
    }

    private String generateExpiredToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 172800000)) // 2 days ago
                .expiration(new Date(System.currentTimeMillis() - 86400000)) // 1 day ago (expired)
                .signWith(key)
                .compact();
    }
}
