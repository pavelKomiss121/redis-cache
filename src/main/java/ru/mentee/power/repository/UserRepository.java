package ru.mentee.power.repository;

import java.util.Optional;

/**
 * JDBC репозиторий пользователей.
 */
public interface UserRepository {

    /**
     * Находит пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return Optional с пользователем
     */
    Optional<User> findById(Long id);

    /**
     * Сохраняет пользователя.
     *
     * @param user пользователь для сохранения
     * @return сохраненный пользователь
     */
    User save(User user);

    /**
     * Удаляет пользователя по ID.
     *
     * @param id идентификатор пользователя
     */
    void deleteById(Long id);
}
