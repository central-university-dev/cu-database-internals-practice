package ru.tbank.di.ast.node;

import java.util.List;

public class SelectStmt extends AstNode {
    public List<ResTarget> targetList;     // что выбираем
    public List<RangeVar> fromClause;      // откуда выбираем
    public Expr whereClause;               // условие (может быть null)

    public SelectStmt(List<ResTarget> targets, List<RangeVar> from, Expr where) {
        this.targetList = targets;
        this.fromClause = from;
        this.whereClause = where;
    }

    @Override
    public String toString() {
        return "......";
    }
}