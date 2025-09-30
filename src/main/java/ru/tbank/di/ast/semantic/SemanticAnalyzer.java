package ru.tbank.di.ast.semantic;

import ru.tbank.di.ast.node.AstNode;


public interface SemanticAnalyzer {
    void analyze(AstNode ast) throws SemanticException;

    class SemanticException extends Exception {
        public SemanticException(String message) {
            super(message);
        }
    }
}
