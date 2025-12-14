package ru.mentee.power.cache;

import lombok.Builder;
import lombok.Getter;

/**
 * Статистика использования кэша.
 */
@Getter
@Builder
public class CacheStatistics {
    private final long hits;
    private final long misses;
    private final double hitRatio;
    private final long averageEntrySize;
    private final long memoryUsage;
}
