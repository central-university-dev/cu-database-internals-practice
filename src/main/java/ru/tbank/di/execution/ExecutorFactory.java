package ru.tbank.di.execution;

import ru.tbank.di.execution.executor.Executor;
import ru.tbank.di.optimizer.node.PhysicalPlanNode;

public interface ExecutorFactory {
    Executor createExecutor(PhysicalPlanNode plan);
}
