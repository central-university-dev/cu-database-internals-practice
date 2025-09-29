package ru.tbank.di.catalog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Table {
    private final String name;
    private final List<Column> columns;
    private final TableDefinition definition;

    public Table(String name, List<Column> columns) {
        this(name, columns, null);
    }

    Table(String name, List<Column> columns, TableDefinition definition) {
        Objects.requireNonNull(name, "name");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(columns, "columns");
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("columns must not be empty");
        }
        this.name = name;
        this.columns = List.copyOf(columns);
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public TableDefinition getDefinition() {
        return definition;
    }
}
