package ru.tbank.di.analyzer;

import ru.tbank.di.ast.node.Expr;

import java.util.List;


public class Query {
    public List<RangeTblEntry> rangeTable;  // таблицы, участвующие в запросе
    public List<TargetEntry> targetList;    // что выбираем
    public Expr whereClause;                // условие отбора
    public QueryType commandType;           // SELECT, INSERT, etc.

}
