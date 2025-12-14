package ru.mentee.power.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Конфигурация кэширования.
 */
@Getter
@Builder
public class CacheConfig {
    private final int l1MaxSize;
    private final int l1TTL;
    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;
    private final int redisMaxConnections;
    private final int redisMaxIdle;
    private final int redisMinIdle;
    private final int defaultTTL;
}
