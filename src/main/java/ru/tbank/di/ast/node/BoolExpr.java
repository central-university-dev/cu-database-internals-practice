package ru.tbank.di.ast.node;

import java.util.List;

public class BoolExpr extends Expr {
    public String boolop;      // "AND", "OR", "NOT"
    public List<Expr> args;    // аргументы

    public BoolExpr(String op, List<Expr> arguments) {
        this.boolop = op;
        this.args = arguments;
    }

    @Override
    public String toString() {
        return "......";
    }
}