package ru.tbank.di;

import ru.tbank.di.buffer.MinimalBufferManager;
import ru.tbank.di.catalog.*;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Path baseDir = Path.of("demo-db");

        MinimalBufferManager bufferManager = new MinimalBufferManager(32);
        CatalogManager catalog = new CatalogManager(bufferManager, baseDir);
        OperationManager operations = new OperationManager(catalog);

        Type intType = catalog.getType("INT");
        Type varcharType = catalog.getType("VARCHAR_256");

        Table users = new Table(
                "users",
                List.of(
                        new Column("id", intType),
                        new Column("name", varcharType)
                )
        );

        try {
            catalog.createTable(users);
        } catch (IllegalArgumentException ignored) {
            // Table already exists, reuse it.
        }

        operations.insert("users", 4, "Egor");
        operations.insert("users", 2, "Bob");
        operations.insert("users", 3, "Charlie");

        Table table = catalog.getTable("users");
        System.out.println("Registered tables: " + catalog.listTables());
        System.out.println("Columns for table '" + table.getName() + "':");
        table.getColumns().forEach(column ->
                System.out.println("  - " + column.getName() + " (" + column.getType().getName() + ")")
        );

        System.out.println("\nRows in 'users':");
        List<List<Object>> rows = operations.get("users");
        rows.stream()
                .sorted(Comparator.comparing(row -> (Integer) row.get(0)))
                .forEach(row -> System.out.println("  " + row));

        bufferManager.flushAllDirty();
    }
}
