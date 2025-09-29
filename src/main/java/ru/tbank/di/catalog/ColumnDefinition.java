package ru.tbank.di.catalog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record ColumnDefinition(int tableOid, int typeOid, String name, int position) {

    public ColumnDefinition {
        if (tableOid < 0 || typeOid < 0) {
            throw new IllegalArgumentException("tableOid and typeOid must be non-negative");
        }
        Objects.requireNonNull(name, "name");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative");
        }
    }

    public byte[] toBytes() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length > Short.MAX_VALUE) {
            throw new IllegalStateException("column name is too long");
        }

        ByteBuffer buffer = ByteBuffer
                .allocate(4 + 4 + 4 + 2 + nameBytes.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(tableOid);
        buffer.putInt(typeOid);
        buffer.putInt(position);
        buffer.putShort((short) nameBytes.length);
        buffer.put(nameBytes);
        return buffer.array();
    }

    public static ColumnDefinition fromBytes(byte[] raw) {
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.remaining() < 4 + 4 + 4 + 2) {
            throw new IllegalArgumentException("payload is too small for ColumnDefinition");
        }
        int tableOid = buffer.getInt();
        int typeOid = buffer.getInt();
        int position = buffer.getInt();
        int nameLen = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() != nameLen) {
            throw new IllegalArgumentException("invalid payload for ColumnDefinition");
        }
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        return new ColumnDefinition(tableOid, typeOid, name, position);
    }
}
