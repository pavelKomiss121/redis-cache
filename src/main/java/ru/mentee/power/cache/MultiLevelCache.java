package ru.mentee.power.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ru.mentee.power.config.CacheConfig;
import ru.mentee.power.serializer.JsonSerializer;
import ru.mentee.power.serializer.Serializer;

/**
 * Многоуровневый кэш (L1: Caffeine, L2: Redis).
 */
@Slf4j
public class MultiLevelCache implements CacheService {

    private final Cache<String, String> l1Cache;
    private final JedisPool jedisPool;
    private final Serializer serializer;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    public MultiLevelCache(CacheConfig config) {
        // L1: In-memory cache
        this.l1Cache =
                Caffeine.newBuilder()
                        .maximumSize(config.getL1MaxSize())
                        .expireAfterWrite(config.getL1TTL(), TimeUnit.SECONDS)
                        .recordStats()
                        .build();

        // L2: Redis
        redis.clients.jedis.JedisPoolConfig poolConfig = new redis.clients.jedis.JedisPoolConfig();
        poolConfig.setMaxTotal(config.getRedisMaxConnections());
        poolConfig.setMaxIdle(config.getRedisMaxIdle());
        poolConfig.setMinIdle(config.getRedisMinIdle());
        poolConfig.setTestOnBorrow(true);

        this.jedisPool =
                new JedisPool(
                        poolConfig,
                        config.getRedisHost(),
                        config.getRedisPort(),
                        2000,
                        config.getRedisPassword().isEmpty() ? null : config.getRedisPassword());

        this.serializer = new JsonSerializer();
    }

    @Override
    public <T> T get(String key, Class<T> type, Supplier<T> loader, int ttl) {
        // 1. Проверяем L1 кэш
        String l1Value = l1Cache.getIfPresent(key);
        if (l1Value != null) {
            log.debug("L1 Cache HIT: {}", key);
            hits.incrementAndGet();
            return deserialize(l1Value, type);
        }

        // 2. Проверяем L2 кэш (Redis)
        try (Jedis jedis = jedisPool.getResource()) {
            String l2Value = jedis.get(key);
            if (l2Value != null) {
                log.debug("L2 Cache HIT: {}", key);
                hits.incrementAndGet();
                // Сохраняем в L1 для следующих запросов
                l1Cache.put(key, l2Value);
                return deserialize(l2Value, type);
            }
        } catch (Exception e) {
            log.error("Ошибка работы с Redis: {}", e.getMessage());
        }

        // 3. Cache MISS - загружаем данные
        log.debug("Cache MISS: {}", key);
        misses.incrementAndGet();
        T value = loader.get();
        if (value != null) {
            put(key, value, ttl);
        }
        return value;
    }

    @Override
    public <T> void put(String key, T value, int ttl) {
        String serialized = serializer.serialize(value);
        // Сохраняем в L1
        l1Cache.put(key, serialized);
        // Сохраняем в L2
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, ttl, serialized);
        } catch (Exception e) {
            log.error("Ошибка сохранения в Redis: {}", e.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        l1Cache.invalidate(key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            log.error("Ошибка удаления из Redis: {}", e.getMessage());
        }
    }

    @Override
    public void evictByPattern(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                keys.forEach(l1Cache::invalidate);
                jedis.del(keys.toArray(new String[0]));
                log.debug("Удалено {} ключей по паттерну: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления по паттерну: {}", e.getMessage());
        }
    }

    @Override
    public CacheStatistics getStatistics() {
        CacheStats stats = l1Cache.stats();
        long totalRequests = hits.get() + misses.get();
        double hitRatio = totalRequests > 0 ? (double) hits.get() / totalRequests : 0.0;

        return CacheStatistics.builder()
                .hits(hits.get())
                .misses(misses.get())
                .hitRatio(hitRatio)
                .averageEntrySize(0) // Упрощенная реализация
                .memoryUsage(stats.evictionCount())
                .build();
    }

    private <T> T deserialize(String data, Class<T> type) {
        return serializer.deserialize(data, type);
    }
}
