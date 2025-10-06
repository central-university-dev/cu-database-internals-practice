package ru.tbank.di.optimizer;

import ru.tbank.di.optimizer.node.PhysicalPlanNode;
import ru.tbank.di.planner.node.LogicalPlanNode;

public interface Optimizer {
    // В этой итерации будет "тупой" маппинг логического плана на физический,
    //    оптимизации различных видов разберем на отдельной лекции.
    // Прямое преобразование узлов в физические - переложите одну ДТО на другую
    PhysicalPlanNode optimize(LogicalPlanNode logicalPlan);
}