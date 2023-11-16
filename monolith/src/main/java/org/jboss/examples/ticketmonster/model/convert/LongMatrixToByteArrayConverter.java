package org.jboss.examples.ticketmonster.model.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Converter(autoApply = true)
public class LongMatrixToByteArrayConverter implements AttributeConverter<long[][], byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(long[][] attribute) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(attribute);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error converting long[][] to byte[]", e);
        }
    }

    @Override
    public long[][] convertToEntityAttribute(byte[] dbData) {
        try {
            // Deserialize the byte[] back to long[][]
            // (This assumes that the stored data is in the same format)
            // Caution: This might throw exceptions if the stored data is corrupted or of different format
            return (long[][]) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(dbData)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error converting byte[] to long[][]", e);
        }
    }
}
