package ru.tbank.di.catalog;

import java.util.Objects;

public final class Column {
    private final String name;
    private final Type type;
    private final int position;
    private final ColumnDefinition definition;

    public Column(String name, Type type) {
        this(name, type, -1, null);
    }

    Column(String name, Type type, int position, ColumnDefinition definition) {
        Objects.requireNonNull(name, "name");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(type, "type");
        if (position < -1) {
            throw new IllegalArgumentException("position must be >= -1");
        }
        this.name = name;
        this.type = type;
        this.position = position;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public ColumnDefinition getDefinition() {
        return definition;
    }
}
