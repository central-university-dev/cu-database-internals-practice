package ru.tbank.di.execution.executor;

public interface Executor {
    void open();
    Object next();
    void close();
}