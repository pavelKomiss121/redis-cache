package ru.mentee.power.cache;

import ru.mentee.power.util.RedisKeyGenerator;

/**
 * Кэш товаров.
 */
public class ProductCache {

    private final CacheService cacheService;
    private static final int DEFAULT_TTL = 3600;

    public ProductCache(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public <T> T getProduct(Long id, Class<T> type, java.util.function.Supplier<T> loader) {
        return cacheService.get(RedisKeyGenerator.productKey(id), type, loader, DEFAULT_TTL);
    }

    public <T> void putProduct(Long id, T product) {
        cacheService.put(RedisKeyGenerator.productKey(id), product, DEFAULT_TTL);
    }

    public void evictProduct(Long id) {
        cacheService.evict(RedisKeyGenerator.productKey(id));
    }

    public void evictAllProducts() {
        cacheService.evictByPattern(RedisKeyGenerator.productPattern());
    }
}
