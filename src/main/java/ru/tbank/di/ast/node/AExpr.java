package ru.tbank.di.ast.node;

public class AExpr extends Expr {
    public String op;          // оператор: "=", ">", "+", etc.
    public Expr left;          // левый операнд
    public Expr right;         // правый операнд

    public AExpr(String operator, Expr leftExpr, Expr rightExpr) {
        this.op = operator;
        this.left = leftExpr;
        this.right = rightExpr;
    }

    @Override
    public String toString() {
        return "......";
    }
}