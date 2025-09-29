package ru.tbank.di.catalog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record TableDefinition(int oid, String name, String relationType, String fileNode, int pages) {

    public static final String DEFAULT_RELATION_TYPE = "table";

    public TableDefinition {
        if (oid < 0) {
            throw new IllegalArgumentException("oid must be non-negative");
        }
        Objects.requireNonNull(name, "name");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(relationType, "relationType");
        relationType = relationType.strip();
        if (relationType.isEmpty()) {
            throw new IllegalArgumentException("relationType must not be blank");
        }
        Objects.requireNonNull(fileNode, "fileNode");
        fileNode = fileNode.strip();
        if (fileNode.isEmpty()) {
            throw new IllegalArgumentException("fileNode must not be blank");
        }
        if (pages < 0) {
            throw new IllegalArgumentException("pages must be non-negative");
        }
    }

    public TableDefinition withPages(int newPages) {
        return new TableDefinition(oid, name, relationType, fileNode, newPages);
    }

    public byte[] toBytes() {
        byte[] nameBytes = toBytes(name);
        byte[] typeBytes = toBytes(relationType);
        byte[] fileNodeBytes = toBytes(fileNode);

        ByteBuffer buffer = ByteBuffer
                .allocate(4 + 4 + 2 + nameBytes.length + 2 + typeBytes.length + 2 + fileNodeBytes.length)
                .order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(oid);
        buffer.putInt(pages);
        buffer.putShort((short) nameBytes.length);
        buffer.put(nameBytes);
        buffer.putShort((short) typeBytes.length);
        buffer.put(typeBytes);
        buffer.putShort((short) fileNodeBytes.length);
        buffer.put(fileNodeBytes);
        return buffer.array();
    }

    public static TableDefinition fromBytes(byte[] raw) {
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.remaining() < 4 + 4 + 2 + 2 + 2) {
            throw new IllegalArgumentException("payload is too small for TableDefinition");
        }

        int oid = buffer.getInt();
        int pages = buffer.getInt();
        String name = readString(buffer);
        String relationType = readString(buffer);
        String fileNode = readString(buffer);
        return new TableDefinition(oid, name, relationType, fileNode, pages);
    }

    private static byte[] toBytes(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > Short.MAX_VALUE) {
            throw new IllegalStateException("string value is too long");
        }
        return bytes;
    }

    private static String readString(ByteBuffer buffer) {
        int len = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() < len) {
            throw new IllegalArgumentException("invalid string payload");
        }
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
