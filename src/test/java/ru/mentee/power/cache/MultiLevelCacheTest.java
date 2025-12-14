package ru.mentee.power.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mentee.power.config.CacheConfig;
import ru.mentee.power.repository.Product;
import ru.mentee.power.repository.User;

/**
 * Тестирование MultiLevelCache.
 */
@DisplayName("Тестирование MultiLevelCache")
@Testcontainers
class MultiLevelCacheTest {

    @Container
    private static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private MultiLevelCache cache;

    @BeforeEach
    void setUp() {
        CacheConfig config =
                CacheConfig.builder()
                        .l1MaxSize(100)
                        .l1TTL(60)
                        .redisHost(redisContainer.getHost())
                        .redisPort(redisContainer.getMappedPort(6379))
                        .redisPassword("")
                        .redisMaxConnections(10)
                        .redisMaxIdle(5)
                        .redisMinIdle(1)
                        .defaultTTL(3600)
                        .build();

        cache = new MultiLevelCache(config);
        // Очищаем Redis перед каждым тестом
        try (redis.clients.jedis.Jedis jedis =
                new redis.clients.jedis.Jedis(
                        redisContainer.getHost(), redisContainer.getMappedPort(6379))) {
            jedis.flushAll();
        }
    }

    @Test
    @DisplayName("Should использовать L1 кэш при повторном запросе")
    void shouldUseL1CacheOnSecondRequest() {
        // Given
        String key = "test:l1:user:1";
        User user = new User(1L, "Test User", "test@example.com");
        AtomicInteger loaderCalls = new AtomicInteger(0);

        // When - первый запрос
        User result1 =
                cache.get(
                        key,
                        User.class,
                        () -> {
                            loaderCalls.incrementAndGet();
                            return user;
                        },
                        3600);

        // And - второй запрос
        User result2 =
                cache.get(
                        key,
                        User.class,
                        () -> {
                            loaderCalls.incrementAndGet();
                            return user;
                        },
                        3600);

        // Then
        assertThat(result1).isEqualTo(user);
        assertThat(result2).isEqualTo(user);
        assertThat(loaderCalls.get()).isEqualTo(1); // loader вызван только раз
    }

    @Test
    @DisplayName("Should инвалидировать кэш по паттерну")
    void shouldEvictByPattern() {
        // Given
        cache.put("user:1", new User(1L, "User 1", "user1@example.com"), 3600);
        cache.put("user:2", new User(2L, "User 2", "user2@example.com"), 3600);
        cache.put("product:1", new Product(1L, "Product 1"), 3600);

        // When
        cache.evictByPattern("user:*");

        // Then
        assertThat(cache.get("user:1", User.class, () -> null, 0)).isNull();
        assertThat(cache.get("user:2", User.class, () -> null, 0)).isNull();
        assertThat(cache.get("product:1", Product.class, () -> null, 0)).isNotNull();
    }

    @Test
    @DisplayName("Should возвращать статистику кэша")
    void shouldReturnCacheStatistics() {
        // Given
        String key = "test:stats:user:1";
        User user = new User(1L, "Test User", "test@example.com");

        // When
        cache.get(key, User.class, () -> user, 3600);
        cache.get(key, User.class, () -> user, 3600);
        CacheStatistics stats = cache.getStatistics();

        // Then
        assertThat(stats.getHits()).isGreaterThan(0);
        assertThat(stats.getHitRatio()).isGreaterThan(0.0);
    }
}
