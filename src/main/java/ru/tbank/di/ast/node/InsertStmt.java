package ru.tbank.di.ast.node;

import java.util.List;

public class InsertStmt extends AstNode {
    public RangeVar relation;              // таблица
    public List<ColumnRef> cols;           // столбцы
    public List<AConst> vals;              // значения

    public InsertStmt(RangeVar table, List<ColumnRef> columns, List<AConst> values) {
        this.relation = table;
        this.cols = columns;
        this.vals = values;
    }

    @Override
    public String toString() {
        return "......";
    }
}