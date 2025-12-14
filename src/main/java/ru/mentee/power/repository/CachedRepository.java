package ru.mentee.power.repository;

import java.util.List;
import java.util.Optional;

/**
 * Декоратор для репозиториев с поддержкой кэширования.
 */
public interface CachedRepository<T, ID> {

    /**
     * Находит сущность по ID с использованием кэша.
     *
     * @param id идентификатор сущности
     * @return Optional с сущностью
     */
    Optional<T> findById(ID id);

    /**
     * Сохраняет сущность и обновляет кэш.
     *
     * @param entity сущность для сохранения
     * @return сохраненная сущность
     */
    T save(T entity);

    /**
     * Удаляет сущность и инвалидирует кэш.
     *
     * @param id идентификатор сущности
     */
    void deleteById(ID id);

    /**
     * Настраивает прогрев кэша.
     *
     * @param ids список ID для прогрева
     */
    void warmUp(List<ID> ids);
}
