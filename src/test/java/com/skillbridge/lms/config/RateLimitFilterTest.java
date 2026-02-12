package com.skillbridge.lms.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
    }

    @Test
    @DisplayName("OPTIONS リクエストはレート制限をスキップ")
    void optionsRequest_skipsRateLimit() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("OPTIONS");

        rateLimitFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("通常リクエストはレート制限ヘッダーを設定")
    void normalRequest_setsRateLimitHeaders() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        rateLimitFilter.doFilterInternal(request, response, chain);

        verify(response).setHeader("X-RateLimit-Limit", "60");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-Forwarded-For ヘッダーからIPを取得")
    void xForwardedFor_usedForIp() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        rateLimitFilter.doFilterInternal(request, response, chain);

        verify(response).setHeader("X-RateLimit-Limit", "60");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("レート制限超過で429を返す")
    void rateLimitExceeded_returns429() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");
        when(response.getWriter()).thenReturn(pw);

        // Exhaust the rate limit
        for (int i = 0; i < 61; i++) {
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        verify(response).setStatus(429);
    }
}
