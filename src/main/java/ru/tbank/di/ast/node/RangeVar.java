package ru.tbank.di.ast.node;

public class RangeVar extends AstNode {
    public String schemaname;  // схема (может быть null)
    public String relname;     // имя таблицы
    public String alias;       // псевдоним (может быть null)

    public RangeVar(String schema, String name, String alias) {
        this.schemaname = schema;
        this.relname = name;
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "......";
    }
}