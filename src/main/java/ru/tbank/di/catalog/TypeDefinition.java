package ru.tbank.di.catalog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record TypeDefinition(int oid, String name, int byteLength, int variableLengthLimit) {

    public TypeDefinition {
        if (oid < 0) {
            throw new IllegalArgumentException("oid must be non-negative");
        }
        Objects.requireNonNull(name, "name");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (byteLength == 0) {
            throw new IllegalArgumentException("byteLength must not be zero");
        }
        if (byteLength < 0 && variableLengthLimit <= 0) {
            throw new IllegalArgumentException("variableLengthLimit must be positive for variable length types");
        }
        if (byteLength > 0 && variableLengthLimit != -1) {
            throw new IllegalArgumentException("variableLengthLimit must be -1 for fixed length types");
        }
    }

    public boolean isVariableLength() {
        return byteLength < 0;
    }

    public byte[] toBytes() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length > Short.MAX_VALUE) {
            throw new IllegalStateException("type name is too long");
        }

        ByteBuffer buffer = ByteBuffer
                .allocate(4 + 4 + 4 + 2 + nameBytes.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(oid);
        buffer.putInt(byteLength);
        buffer.putInt(variableLengthLimit);
        buffer.putShort((short) nameBytes.length);
        buffer.put(nameBytes);
        return buffer.array();
    }

    public static TypeDefinition fromBytes(byte[] raw) {
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.remaining() < 4 + 4 + 4 + 2) {
            throw new IllegalArgumentException("payload is too small for TypeDefinition");
        }

        int oid = buffer.getInt();
        int byteLength = buffer.getInt();
        int variableLengthLimit = buffer.getInt();
        int nameLen = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() != nameLen) {
            throw new IllegalArgumentException("invalid payload for TypeDefinition");
        }
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        return new TypeDefinition(oid, name, byteLength, variableLengthLimit);
    }

    public Type toType() {
        int effectiveByteLength = byteLength;
        int effectiveLimit = variableLengthLimit;
        if (!isVariableLength()) {
            effectiveLimit = -1;
        } else {
            effectiveByteLength = -1;
        }
        return new Type(oid, name, effectiveByteLength, effectiveLimit, this);
    }
}
