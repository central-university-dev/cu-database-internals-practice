package ru.tbank.di.ast.node;

public class ResTarget extends AstNode {
    Expr val;           // выражение
    String name;        // псевдоним (может быть null)

    public ResTarget(Expr expr, String alias) {
        this.val = expr;
        this.name = alias;
    }

    @Override
    public String toString() {
        return "......";
    }
}