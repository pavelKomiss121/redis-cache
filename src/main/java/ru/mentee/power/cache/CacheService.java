package ru.mentee.power.cache;

import java.util.function.Supplier;

/**
 * Универсальный интерфейс для работы с кэшем.
 */
public interface CacheService {

    /**
     * Получает значение из кэша или загружает через loader.
     *
     * @param key ключ кэша
     * @param type тип возвращаемого объекта
     * @param loader функция загрузки при отсутствии в кэше
     * @param ttl время жизни в секундах
     * @return значение из кэша или загруженное
     */
    <T> T get(String key, Class<T> type, Supplier<T> loader, int ttl);

    /**
     * Сохраняет значение в кэш.
     *
     * @param key ключ кэша
     * @param value значение для сохранения
     * @param ttl время жизни в секундах
     */
    <T> void put(String key, T value, int ttl);

    /**
     * Удаляет значение из кэша.
     *
     * @param key ключ для удаления
     */
    void evict(String key);

    /**
     * Удаляет все значения по паттерну.
     *
     * @param pattern паттерн ключей (например, "user:*")
     */
    void evictByPattern(String pattern);

    /**
     * Получает статистику использования кэша.
     *
     * @return статистика кэша
     */
    CacheStatistics getStatistics();
}
