package ru.mentee.power.util;

/**
 * Генератор ключей Redis.
 */
public class RedisKeyGenerator {

    public static String userKey(Long id) {
        return "user:" + id;
    }

    public static String productKey(Long id) {
        return "product:" + id;
    }

    public static String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    public static String cartKey(Long userId) {
        return "cart:" + userId;
    }

    public static String userPattern() {
        return "user:*";
    }

    public static String productPattern() {
        return "product:*";
    }

    public static String sessionPattern() {
        return "session:*";
    }

    public static String cartPattern() {
        return "cart:*";
    }
}
