package ru.tbank.di.execution;

import ru.tbank.di.execution.executor.Executor;

import java.util.List;

public interface QueryExecutionEngine {
    List<Object> execute(Executor executor);
}