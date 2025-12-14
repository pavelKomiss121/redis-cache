package ru.mentee.power.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON сериализация.
 */
@Slf4j
public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public <T> String serialize(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Ошибка сериализации объекта", e);
            throw new RuntimeException("Ошибка сериализации", e);
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (Exception e) {
            log.error("Ошибка десериализации объекта", e);
            throw new RuntimeException("Ошибка десериализации", e);
        }
    }
}
