package ru.mentee.power.cache;

import ru.mentee.power.util.RedisKeyGenerator;

/**
 * Кэш сессий.
 */
public class SessionCache {

    private final CacheService cacheService;
    private static final int DEFAULT_TTL = 3600;

    public SessionCache(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public <T> T getSession(
            String sessionId, Class<T> type, java.util.function.Supplier<T> loader) {
        return cacheService.get(RedisKeyGenerator.sessionKey(sessionId), type, loader, DEFAULT_TTL);
    }

    public <T> void putSession(String sessionId, T session) {
        cacheService.put(RedisKeyGenerator.sessionKey(sessionId), session, DEFAULT_TTL);
    }

    public void evictSession(String sessionId) {
        cacheService.evict(RedisKeyGenerator.sessionKey(sessionId));
    }

    public void evictAllSessions() {
        cacheService.evictByPattern(RedisKeyGenerator.sessionPattern());
    }
}
