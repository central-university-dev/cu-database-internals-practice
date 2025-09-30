package ru.tbank.di.ast.node;

public class TypeName extends AstNode {
    public String name;        // имя типа: "INTEGER", "TEXT", etc.

    public TypeName(String typeName) {
        this.name = typeName;
    }

    @Override
    public String toString() {
        return "......";
    }
}