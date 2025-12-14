package ru.mentee.power.cache;

import ru.mentee.power.util.RedisKeyGenerator;

/**
 * Кэш пользователей.
 */
public class UserCache {

    private final CacheService cacheService;
    private static final int DEFAULT_TTL = 3600;

    public UserCache(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public <T> T getUser(Long id, Class<T> type, java.util.function.Supplier<T> loader) {
        return cacheService.get(RedisKeyGenerator.userKey(id), type, loader, DEFAULT_TTL);
    }

    public <T> void putUser(Long id, T user) {
        cacheService.put(RedisKeyGenerator.userKey(id), user, DEFAULT_TTL);
    }

    public void evictUser(Long id) {
        cacheService.evict(RedisKeyGenerator.userKey(id));
    }

    public void evictAllUsers() {
        cacheService.evictByPattern(RedisKeyGenerator.userPattern());
    }
}
