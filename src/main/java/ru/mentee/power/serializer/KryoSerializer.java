package ru.mentee.power.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * Kryo сериализация.
 */
@Slf4j
public class KryoSerializer implements Serializer {

    private final ThreadLocal<Kryo> kryoThreadLocal;

    public KryoSerializer() {
        this.kryoThreadLocal =
                ThreadLocal.withInitial(
                        () -> {
                            Kryo kryo = new Kryo();
                            kryo.setRegistrationRequired(false);
                            return kryo;
                        });
    }

    @Override
    public <T> String serialize(T object) {
        try {
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (Output output = new Output(outputStream)) {
                kryo.writeObject(output, object);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Ошибка сериализации объекта", e);
            throw new RuntimeException("Ошибка сериализации", e);
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        try {
            Kryo kryo = kryoThreadLocal.get();
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try (Input input = new Input(inputStream)) {
                return kryo.readObject(input, type);
            }
        } catch (Exception e) {
            log.error("Ошибка десериализации объекта", e);
            throw new RuntimeException("Ошибка десериализации", e);
        }
    }
}
