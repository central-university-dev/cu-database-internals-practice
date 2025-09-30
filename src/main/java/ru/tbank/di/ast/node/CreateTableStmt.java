package ru.tbank.di.ast.node;

import java.util.List;

public class CreateTableStmt extends AstNode {
    public RangeVar relation;              // имя таблицы
    public List<ColumnDef> tableElts;      // определения столбцов

    public CreateTableStmt(RangeVar table, List<ColumnDef> columns) {
        this.relation = table;
        this.tableElts = columns;
    }

    @Override
    public String toString() {
        return "......";
    }
}