package ru.tbank.di.ast.builder;

import ru.tbank.di.ast.node.BoolExpr;
import ru.tbank.di.ast.node.Expr;

public class WhereBuilder {

    public static BoolExpr buildComplexWhere() {
        // TODO: построить дерево для: age > 20 OR NOT city = 'SPB'

        return null;
    }

    // Метод для красивого вывода дерева
    public static void printWhereTree(Expr node, int indent) {
        // TODO: рекурсивный вывод с отступами
    }
}