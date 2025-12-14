package ru.mentee.power.repository;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ru.mentee.power.cache.CacheService;
import ru.mentee.power.util.RedisKeyGenerator;

/**
 * Декоратор для репозитория пользователей с поддержкой кэширования.
 */
@Slf4j
public class CachedUserRepository implements CachedRepository<User, Long> {

    private final UserRepository userRepository;
    private final CacheService cacheService;
    private static final int DEFAULT_TTL = 3600;

    public CachedUserRepository(UserRepository userRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Override
    public Optional<User> findById(Long id) {
        String key = RedisKeyGenerator.userKey(id);
        User user =
                cacheService.get(
                        key,
                        User.class,
                        () -> {
                            Optional<User> found = userRepository.findById(id);
                            return found.orElse(null);
                        },
                        DEFAULT_TTL);
        return Optional.ofNullable(user);
    }

    @Override
    public User save(User entity) {
        User saved = userRepository.save(entity);
        if (saved != null && saved.getId() != null) {
            cacheService.put(RedisKeyGenerator.userKey(saved.getId()), saved, DEFAULT_TTL);
        }
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
        cacheService.evict(RedisKeyGenerator.userKey(id));
    }

    @Override
    public void warmUp(List<Long> ids) {
        log.info("Прогрев кэша для {} пользователей", ids.size());
        ids.forEach(id -> findById(id));
    }
}
