package ru.mentee.power.serializer;

/**
 * Интерфейс сериализатора.
 */
public interface Serializer {

    /**
     * Сериализует объект в строку.
     *
     * @param object объект для сериализации
     * @return сериализованная строка
     */
    <T> String serialize(T object);

    /**
     * Десериализует строку в объект.
     *
     * @param data сериализованная строка
     * @param type тип объекта
     * @return десериализованный объект
     */
    <T> T deserialize(String data, Class<T> type);
}
