package ru.tbank.di.catalog;

import ru.tbank.di.buffer.MinimalBufferManager;
import ru.tbank.di.buffer.PageAddress;
import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CatalogManager {
    private static final int SLOT_OVERHEAD = 4;
    private static final String DEFAULT_CATALOG_DIR = "catalog";
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String TABLE_DEFINITIONS_FILE = "table_definitions.dat";
    private static final String COLUMN_DEFINITIONS_FILE = "column_definitions.dat";
    private static final String TYPE_DEFINITIONS_FILE = "types_definitions.dat";

    private final Path dataDir;
    private final MinimalBufferManager bufferManager;
    private final Path typeCatalogFile;
    private final Path tableCatalogFile;
    private final Path columnCatalogFile;

    private final Map<Integer, TypeDefinition> typesByOid = new HashMap<>();
    private final Map<String, TypeDefinition> typesByName = new HashMap<>();
    private final Map<Integer, RecordPointer> typePointers = new HashMap<>();

    private final Map<Integer, TableDefinition> tablesByOid = new HashMap<>();
    private final Map<String, TableDefinition> tablesByName = new HashMap<>();
    private final Map<Integer, RecordPointer> tablePointers = new HashMap<>();

    private final Map<Integer, List<ColumnDefinition>> columnsByTable = new HashMap<>();

    private int nextTypeOid = 1;
    private int nextTableOid = 1;

    public CatalogManager() throws IOException {
        this(new MinimalBufferManager(), Path.of("."));
    }

    public CatalogManager(MinimalBufferManager bufferManager) throws IOException {
        this(bufferManager, Path.of("."));
    }

    public CatalogManager(MinimalBufferManager bufferManager, Path baseDir) throws IOException {
        this.bufferManager = Objects.requireNonNull(bufferManager, "bufferManager");
        Objects.requireNonNull(baseDir, "baseDir");
        Path normalizedBase = baseDir.toAbsolutePath().normalize();
        Path catalogDir = normalizedBase.resolve(DEFAULT_CATALOG_DIR);
        this.dataDir = normalizedBase.resolve(DEFAULT_DATA_DIR);

        Files.createDirectories(catalogDir);
        Files.createDirectories(dataDir);

        this.typeCatalogFile = prepareCatalogFile(catalogDir.resolve(TYPE_DEFINITIONS_FILE));
        this.tableCatalogFile = prepareCatalogFile(catalogDir.resolve(TABLE_DEFINITIONS_FILE));
        this.columnCatalogFile = prepareCatalogFile(catalogDir.resolve(COLUMN_DEFINITIONS_FILE));

        loadTypes();
        loadTables();
        loadColumns();
        ensureBuiltinTypes();
    }

    public MinimalBufferManager getBufferManager() {
        return bufferManager;
    }

    public synchronized void createTable(Table table) throws IOException {
        Objects.requireNonNull(table, "table");
        String normalizedTableName = normalize(table.getName());
        if (tablesByName.containsKey(normalizedTableName)) {
            throw new IllegalArgumentException("table already exists: " + table.getName());
        }

        List<Column> columns = table.getColumns();
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("table must contain columns");
        }

        Set<String> seenColumns = new java.util.HashSet<>();
        List<ColumnDefinition> newColumnDefinitions = new ArrayList<>();

        int tableOid = nextTableOid++;
        String fileNode = tableOid + ".dat";
        TableDefinition tableDefinition = new TableDefinition(
                tableOid,
                table.getName(),
                TableDefinition.DEFAULT_RELATION_TYPE,
                fileNode,
                0
        );

        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            String columnKey = normalize(column.getName());
            if (!seenColumns.add(columnKey)) {
                throw new IllegalArgumentException("duplicate column: " + column.getName());
            }

            Type resolvedType = resolveOrCreateType(column.getType());
            ColumnDefinition definition = new ColumnDefinition(
                    tableOid,
                    resolvedType.getDefinition().oid(),
                    column.getName(),
                    i
            );
            newColumnDefinitions.add(definition);
        }

        RecordPointer tablePointer = appendRecord(tableCatalogFile, tableDefinition.toBytes());
        tablesByOid.put(tableDefinition.oid(), tableDefinition);
        tablesByName.put(normalizedTableName, tableDefinition);
        tablePointers.put(tableDefinition.oid(), tablePointer);

        List<ColumnDefinition> storedColumns = columnsByTable.computeIfAbsent(tableDefinition.oid(), key -> new ArrayList<>());
        for (ColumnDefinition columnDefinition : newColumnDefinitions) {
            appendRecord(columnCatalogFile, columnDefinition.toBytes());
            storedColumns.add(columnDefinition);
        }
        storedColumns.sort(Comparator.comparingInt(ColumnDefinition::position));

        Path dataFile = dataDir.resolve(fileNode);
        if (!Files.exists(dataFile)) {
            Files.createFile(dataFile);
        }
    }

    public synchronized Table getTable(String name) {
        Objects.requireNonNull(name, "name");
        TableDefinition tableDefinition = tablesByName.get(normalize(name));
        if (tableDefinition == null) {
            throw new IllegalArgumentException("table not found: " + name);
        }

        List<ColumnDefinition> columnDefinitions = columnsByTable.getOrDefault(
                tableDefinition.oid(),
                List.of()
        );

        List<Column> columns = columnDefinitions.stream()
                .sorted(Comparator.comparingInt(ColumnDefinition::position))
                .map(def -> {
                    TypeDefinition typeDefinition = typesByOid.get(def.typeOid());
                    Type type = typeDefinition.toType();
                    return new Column(def.name(), type, def.position(), def);
                })
                .collect(Collectors.toList());

        if (columns.isEmpty()) {
            throw new IllegalStateException("catalog corrupted: table has no columns");
        }

        return new Table(tableDefinition.name(), columns, tableDefinition);
    }

    public synchronized Set<String> listTables() {
        return tablesByOid.values().stream()
                .sorted(Comparator.comparing(TableDefinition::name, String.CASE_INSENSITIVE_ORDER))
                .map(TableDefinition::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public synchronized Type getType(String name) {
        Objects.requireNonNull(name, "name");
        TypeDefinition definition = typesByName.get(normalize(name));
        if (definition == null) {
            throw new IllegalArgumentException("type not found: " + name);
        }
        return definition.toType();
    }

    synchronized TableDefinition getTableDefinition(String name) {
        return tablesByName.get(normalize(name));
    }

    synchronized List<ColumnDefinition> getColumnDefinitions(int tableOid) {
        List<ColumnDefinition> definitions = columnsByTable.get(tableOid);
        if (definitions == null) {
            return List.of();
        }
        return List.copyOf(definitions);
    }

    synchronized TypeDefinition getTypeDefinition(int typeOid) {
        return typesByOid.get(typeOid);
    }

    synchronized Path resolveDataFile(TableDefinition tableDefinition) {
        return dataDir.resolve(tableDefinition.fileNode());
    }

    synchronized void updateTablePages(int tableOid, int newPages) throws IOException {
        TableDefinition current = tablesByOid.get(tableOid);
        if (current == null) {
            throw new IllegalArgumentException("table oid not found: " + tableOid);
        }
        TableDefinition updated = current.withPages(newPages);
        RecordPointer pointer = tablePointers.get(tableOid);
        if (pointer == null) {
            throw new IllegalStateException("table pointer missing for oid: " + tableOid);
        }
        updateRecord(pointer, updated.toBytes());
        tablesByOid.put(tableOid, updated);
        tablesByName.put(normalize(updated.name()), updated);
    }

    private void loadTypes() throws IOException {
        forEachRecord(typeCatalogFile, (address, slot, payload) -> {
            TypeDefinition definition = TypeDefinition.fromBytes(payload);
            typesByOid.put(definition.oid(), definition);
            typesByName.put(normalize(definition.name()), definition);
            typePointers.put(definition.oid(), new RecordPointer(address, slot));
        });
        nextTypeOid = nextId(typesByOid);
    }

    private void loadTables() throws IOException {
        forEachRecord(tableCatalogFile, (address, slot, payload) -> {
            TableDefinition definition = TableDefinition.fromBytes(payload);
            tablesByOid.put(definition.oid(), definition);
            tablesByName.put(normalize(definition.name()), definition);
            tablePointers.put(definition.oid(), new RecordPointer(address, slot));
        });
        nextTableOid = nextId(tablesByOid);
    }

    private void loadColumns() throws IOException {
        forEachRecord(columnCatalogFile, (address, slot, payload) -> {
            ColumnDefinition definition = ColumnDefinition.fromBytes(payload);
            columnsByTable
                    .computeIfAbsent(definition.tableOid(), key -> new ArrayList<>())
                    .add(definition);
        });
        columnsByTable.values().forEach(list ->
                list.sort(Comparator.comparingInt(ColumnDefinition::position))
        );
    }

    private void ensureBuiltinTypes() throws IOException {
        ensureType("INT", 4, -1);
        ensureType("VARCHAR_256", -1, 256);
    }

    private Type resolveOrCreateType(Type type) throws IOException {
        if (type.getDefinition() != null) {
            return type.getDefinition().toType();
        }
        String normalizedName = normalize(type.getName());
        TypeDefinition existing = typesByName.get(normalizedName);
        if (existing != null) {
            validateCompatible(existing, type);
            return existing.toType();
        }
        int byteLength = type.getByteLength();
        int limit = type.isVariableLength() ? type.getVariableLengthLimit() : -1;
        TypeDefinition definition = new TypeDefinition(nextTypeOid++, type.getName(), byteLength, limit);
        RecordPointer pointer = appendRecord(typeCatalogFile, definition.toBytes());
        typesByOid.put(definition.oid(), definition);
        typesByName.put(normalizedName, definition);
        typePointers.put(definition.oid(), pointer);
        return definition.toType();
    }

    private void ensureType(String name, int byteLength, int variableLengthLimit) throws IOException {
        String normalizedName = normalize(name);
        TypeDefinition existing = typesByName.get(normalizedName);
        if (existing != null) {
            if (existing.byteLength() != byteLength || existing.variableLengthLimit() != variableLengthLimit) {
                throw new IllegalStateException("conflicting built-in type definition: " + name);
            }
            return;
        }
        TypeDefinition definition = new TypeDefinition(nextTypeOid++, name, byteLength, variableLengthLimit);
        RecordPointer pointer = appendRecord(typeCatalogFile, definition.toBytes());
        typesByOid.put(definition.oid(), definition);
        typesByName.put(normalizedName, definition);
        typePointers.put(definition.oid(), pointer);
    }

    private static void validateCompatible(TypeDefinition definition, Type type) {
        if (definition.isVariableLength() != type.isVariableLength()) {
            throw new IllegalArgumentException("type mismatch for " + type.getName());
        }
        if (!definition.isVariableLength() && definition.byteLength() != type.getByteLength()) {
            throw new IllegalArgumentException("type length mismatch for " + type.getName());
        }
        if (definition.isVariableLength() && definition.variableLengthLimit() != type.getVariableLengthLimit()) {
            throw new IllegalArgumentException("type varchar limit mismatch for " + type.getName());
        }
    }

    private static int nextId(Map<Integer, ?> map) {
        return map.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private void forEachRecord(Path file, RecordVisitor visitor) throws IOException {
        int pageCount = pageCount(file);
        for (int pageId = 0; pageId < pageCount; pageId++) {
            PageAddress address = new PageAddress(file, pageId);
            HeapPage page = (HeapPage) bufferManager.get(address);
            int slots = page.getSlotCount();
            for (int slot = 0; slot < slots; slot++) {
                byte[] payload = page.read(slot);
                if (payload != null) {
                    visitor.accept(address, slot, payload);
                }
            }
        }
    }

    private RecordPointer appendRecord(Path file, byte[] payload) throws IOException {
        int pageCount = pageCount(file);
        for (int pageId = 0; pageId < pageCount; pageId++) {
            PageAddress address = new PageAddress(file, pageId);
            HeapPage page = (HeapPage) bufferManager.get(address);
            if (page.getFreeSpace() >= SLOT_OVERHEAD + payload.length) {
                int slot = page.insert(payload);
                bufferManager.write(address, page);
                return new RecordPointer(address, slot);
            }
        }

        int newPageId = pageCount;
        PageAddress address = new PageAddress(file, newPageId);
        HeapPage page = new HeapPage(newPageId);
        int slot = page.insert(payload);
        bufferManager.write(address, page);
        return new RecordPointer(address, slot);
    }

    private void updateRecord(RecordPointer pointer, byte[] payload) throws IOException {
        HeapPage page = (HeapPage) bufferManager.get(pointer.address());
        page.update(pointer.slot(), payload);
        bufferManager.write(pointer.address(), page);
    }

    private int pageCount(Path file) throws IOException {
        long size = Files.size(file);
        if (size % Page.PAGE_SIZE != 0) {
            throw new IllegalStateException("corrupted catalog file: " + file);
        }
        return (int) (size / Page.PAGE_SIZE);
    }

    private static Path prepareCatalogFile(Path file) throws IOException {
        Path normalized = file.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(normalized)) {
            Files.createFile(normalized);
        }
        long size = Files.size(normalized);
        if (size % Page.PAGE_SIZE != 0) {
            throw new IllegalStateException("corrupted catalog file: " + normalized);
        }
        return normalized;
    }

    private record RecordPointer(PageAddress address, int slot) {
    }

    @FunctionalInterface
    private interface RecordVisitor {
        void accept(PageAddress address, int slot, byte[] payload) throws IOException;
    }
}
