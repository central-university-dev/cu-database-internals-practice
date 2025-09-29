package ru.tbank.di.catalog;

import java.util.Objects;

public final class Type {
    private final int oid;
    private final String name;
    private final int byteLength;
    private final int variableLengthLimit;
    private final TypeDefinition definition;

    public Type(String name, int byteLength) {
        this(-1, name, byteLength, -1, null);
    }

    public Type(String name, int byteLength, int variableLengthLimit) {
        this(-1, name, byteLength, variableLengthLimit, null);
    }

    Type(int oid, String name, int byteLength, int variableLengthLimit, TypeDefinition definition) {
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
        this.oid = oid;
        this.name = name;
        this.byteLength = byteLength;
        this.variableLengthLimit = byteLength < 0 ? variableLengthLimit : -1;
        this.definition = definition;
    }

    public int getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }

    public int getByteLength() {
        return byteLength;
    }

    public boolean isVariableLength() {
        return byteLength < 0;
    }

    public int getVariableLengthLimit() {
        return variableLengthLimit;
    }

    public TypeDefinition getDefinition() {
        return definition;
    }
}
