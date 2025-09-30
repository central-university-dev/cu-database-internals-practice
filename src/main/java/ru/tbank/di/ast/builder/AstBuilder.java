package ru.tbank.di.ast.builder;

import ru.tbank.di.ast.node.AstNode;
import ru.tbank.di.ast.node.SelectStmt;

public class AstBuilder {

    // Построить AST для SELECT name FROM users
    public static SelectStmt buildSimpleSelect() {
        // TODO: реализовать
        // 1. Создать ResTarget для столбца "name"
        // 2. Создать RangeVar для таблицы "users"
        // 3. Создать SelectStmt с этими компонентами
        // 4. whereClause = null для простоты

        return null; // заменить на реальную реализацию
    }

    // Вспомогательный метод
    public static void printAst(AstNode node) {
        // TODO: реализовать простой вывод AST
    }
}