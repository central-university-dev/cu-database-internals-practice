package ru.tbank.di.ast.node;

public class ColumnDef extends AstNode {
    public String colname;     // имя столбца
    public TypeName typeName;  // тип

    public ColumnDef(String name, TypeName type) {
        this.colname = name;
        this.typeName = type;
    }

    @Override
    public String toString() {
        return "......";
    }
}