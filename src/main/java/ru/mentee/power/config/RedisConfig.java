package ru.mentee.power.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Конфигурация Redis.
 */
@Slf4j
public class RedisConfig {

    private static final String PROPERTIES_FILE = "redis.properties";

    public JedisPool createJedisPool() {
        Properties props = loadProperties();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(props.getProperty("redis.maxTotal", "10")));
        poolConfig.setMaxIdle(Integer.parseInt(props.getProperty("redis.maxIdle", "5")));
        poolConfig.setMinIdle(Integer.parseInt(props.getProperty("redis.minIdle", "1")));
        poolConfig.setTestOnBorrow(true);

        String host = props.getProperty("redis.host", "localhost");
        int port = Integer.parseInt(props.getProperty("redis.port", "6379"));
        String password = props.getProperty("redis.password", "");

        log.info("Создание пула соединений Redis: {}:{}", host, port);
        return new JedisPool(poolConfig, host, port, 2000, password.isEmpty() ? null : password);
    }

    public CacheConfig createCacheConfig() {
        Properties props = loadProperties();
        return CacheConfig.builder()
                .l1MaxSize(Integer.parseInt(props.getProperty("cache.l1.maxSize", "1000")))
                .l1TTL(Integer.parseInt(props.getProperty("cache.l1.ttl", "60")))
                .redisHost(props.getProperty("redis.host", "localhost"))
                .redisPort(Integer.parseInt(props.getProperty("redis.port", "6379")))
                .redisPassword(props.getProperty("redis.password", ""))
                .redisMaxConnections(Integer.parseInt(props.getProperty("redis.maxTotal", "10")))
                .redisMaxIdle(Integer.parseInt(props.getProperty("redis.maxIdle", "5")))
                .redisMinIdle(Integer.parseInt(props.getProperty("redis.minIdle", "1")))
                .defaultTTL(Integer.parseInt(props.getProperty("cache.default.ttl", "3600")))
                .build();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is != null) {
                props.load(is);
            } else {
                log.warn("Файл {} не найден, используются значения по умолчанию", PROPERTIES_FILE);
            }
        } catch (IOException e) {
            log.error("Ошибка загрузки свойств из {}", PROPERTIES_FILE, e);
        }
        return props;
    }
}
