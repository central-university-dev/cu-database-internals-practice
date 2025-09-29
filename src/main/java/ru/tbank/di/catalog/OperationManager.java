package ru.tbank.di.catalog;

import ru.tbank.di.buffer.MinimalBufferManager;
import ru.tbank.di.buffer.PageAddress;
import ru.tbank.di.memory.HeapPage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OperationManager {
    private static final int SLOT_OVERHEAD = 4;

    private final CatalogManager catalogManager;
    private final MinimalBufferManager bufferManager;

    public OperationManager(CatalogManager catalogManager) {
        this(catalogManager, catalogManager.getBufferManager());
    }

    public OperationManager(CatalogManager catalogManager, MinimalBufferManager bufferManager) {
        this.catalogManager = Objects.requireNonNull(catalogManager, "catalogManager");
        this.bufferManager = Objects.requireNonNull(bufferManager, "bufferManager");
    }

    public synchronized void insert(String tableName, Object... params) throws IOException {
        TableDefinition tableDefinition = requireTable(tableName);
        List<ColumnDefinition> columns = catalogManager.getColumnDefinitions(tableDefinition.oid());
        if (columns.isEmpty()) {
            throw new IllegalStateException("table has no columns: " + tableName);
        }
        if (params.length != columns.size()) {
            throw new IllegalArgumentException("parameter count mismatch for table " + tableName);
        }

        byte[] payload = serialize(columns, params);
        Path dataFile = catalogManager.resolveDataFile(tableDefinition);
        int totalPages = tableDefinition.pages();

        for (int pageId = 0; pageId < totalPages; pageId++) {
            PageAddress address = new PageAddress(dataFile, pageId);
            HeapPage page = (HeapPage) bufferManager.get(address);
            if (page.getFreeSpace() >= SLOT_OVERHEAD + payload.length) {
                page.insert(payload);
                bufferManager.write(address, page);
                return;
            }
        }

        int newPageId = totalPages;
        PageAddress address = new PageAddress(dataFile, newPageId);
        HeapPage newPage = new HeapPage(newPageId);
        newPage.insert(payload);
        bufferManager.write(address, newPage);
        catalogManager.updateTablePages(tableDefinition.oid(), totalPages + 1);
    }

    public synchronized List<List<Object>> get(String tableName) throws IOException {
        TableDefinition tableDefinition = requireTable(tableName);
        List<ColumnDefinition> columns = catalogManager.getColumnDefinitions(tableDefinition.oid());
        if (columns.isEmpty()) {
            return List.of();
        }
        Path dataFile = catalogManager.resolveDataFile(tableDefinition);
        int totalPages = tableDefinition.pages();
        List<List<Object>> result = new ArrayList<>();

        for (int pageId = 0; pageId < totalPages; pageId++) {
            PageAddress address = new PageAddress(dataFile, pageId);
            HeapPage page = (HeapPage) bufferManager.get(address);
            int slots = page.getSlotCount();
            for (int slot = 0; slot < slots; slot++) {
                byte[] raw = page.read(slot);
                if (raw == null) {
                    continue;
                }
                result.add(deserialize(columns, raw));
            }
        }

        return result;
    }

    private TableDefinition requireTable(String tableName) {
        TableDefinition tableDefinition = catalogManager.getTableDefinition(tableName);
        if (tableDefinition == null) {
            throw new IllegalArgumentException("table not found: " + tableName);
        }
        return tableDefinition;
    }

    private byte[] serialize(List<ColumnDefinition> columns, Object[] params) {
        ByteBuffer buffer = ByteBuffer.allocate(estimateSize(columns, params)).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            TypeDefinition typeDefinition = catalogManager.getTypeDefinition(column.typeOid());
            Object value = params[i];
            if (typeDefinition == null) {
                throw new IllegalStateException("missing type definition for column " + column.name());
            }
            if (typeDefinition.isVariableLength()) {
                if (!(value instanceof String str)) {
                    throw new IllegalArgumentException("column " + column.name() + " expects String value");
                }
                byte[] encoded = str.getBytes(StandardCharsets.UTF_8);
                if (encoded.length > typeDefinition.variableLengthLimit()) {
                    throw new IllegalArgumentException("value too long for column " + column.name());
                }
                buffer.putInt(encoded.length);
                buffer.put(encoded);
            } else {
                if (typeDefinition.byteLength() != 4) {
                    throw new UnsupportedOperationException("unsupported fixed type length: " + typeDefinition.byteLength());
                }
                if (!(value instanceof Number number)) {
                    throw new IllegalArgumentException("column " + column.name() + " expects numeric value");
                }
                buffer.putInt(number.intValue());
            }
        }
        return buffer.array();
    }

    private int estimateSize(List<ColumnDefinition> columns, Object[] params) {
        int size = 0;
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            TypeDefinition typeDefinition = catalogManager.getTypeDefinition(column.typeOid());
            if (typeDefinition == null) {
                throw new IllegalStateException("missing type definition for column " + column.name());
            }
            if (typeDefinition.isVariableLength()) {
                Object value = params[i];
                if (!(value instanceof String str)) {
                    throw new IllegalArgumentException("column " + column.name() + " expects String value");
                }
                byte[] encoded = str.getBytes(StandardCharsets.UTF_8);
                if (encoded.length > typeDefinition.variableLengthLimit()) {
                    throw new IllegalArgumentException("value too long for column " + column.name());
                }
                size += 4 + encoded.length;
            } else {
                size += typeDefinition.byteLength();
            }
        }
        return size;
    }

    private List<Object> deserialize(List<ColumnDefinition> columns, byte[] raw) {
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        List<Object> values = new ArrayList<>(columns.size());
        for (ColumnDefinition column : columns) {
            TypeDefinition typeDefinition = catalogManager.getTypeDefinition(column.typeOid());
            if (typeDefinition == null) {
                throw new IllegalStateException("missing type definition for column " + column.name());
            }
            if (typeDefinition.isVariableLength()) {
                int len = buffer.getInt();
                if (len < 0 || len > typeDefinition.variableLengthLimit() || buffer.remaining() < len) {
                    throw new IllegalStateException("corrupted varchar payload for column " + column.name());
                }
                byte[] encoded = new byte[len];
                buffer.get(encoded);
                values.add(new String(encoded, StandardCharsets.UTF_8));
            } else {
                if (typeDefinition.byteLength() != 4 || buffer.remaining() < 4) {
                    throw new IllegalStateException("corrupted int payload for column " + column.name());
                }
                values.add(buffer.getInt());
            }
        }
        if (buffer.hasRemaining()) {
            throw new IllegalStateException("row payload has extra bytes");
        }
        return values;
    }
}
