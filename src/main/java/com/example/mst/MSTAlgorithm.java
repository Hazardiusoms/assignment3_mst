package com.example.mst;

public interface MSTAlgorithm {
    /**
     * Вычисляет MST. Если граф несвязный — вернёт connected=false и пустой/частичный список рёбер,
     * оставляя totalCost равным сумме рёбер в построенной компоненте (обычно 0) — в тестах это будет проверяться.
     */
    MSTResult compute(Graph graph);
}
