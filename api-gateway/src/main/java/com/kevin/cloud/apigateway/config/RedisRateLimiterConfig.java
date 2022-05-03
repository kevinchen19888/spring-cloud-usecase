package com.kevin.cloud.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimiterConfig {
    @Bean
    KeyResolver userKeyResolver() {
        // 根据请求参数中的username进行限流
        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("username"));
    }

    @Primary
    @Bean
    public KeyResolver ipKeyResolver() {
        // 根据访问IP进行限流
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
}