package com.example.mst;

public final class Kruskal implements MSTAlgorithm {

    // Вспомогательная DSU/Union-Find добавлю вместе с реализацией шага Kruskal.
    @Override
    public MSTResult compute(Graph graph) {
        long start = System.nanoTime();
        OperationStats stats = new OperationStats();

        // TODO: Реализация Kruskal:
        //  - сортировка рёбер (учёт stats.addComparisons(...) условно при merge sort/Arrays.sort недоступно — допустим приближённо)
        //  - DSU find/union (stats.incFinds(), stats.incUnions())
        //  - проверка связности и размера MST

        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        return new MSTResult.Builder()
                .algorithm(AlgorithmType.KRUSKAL)
                .mstEdges(java.util.Collections.emptyList())
                .totalCost(0.0)
                .originalVertexCount(graph.vertexCount())
                .originalEdgeCount(graph.edgeCount())
                .durationMillis(durationMs)
                .stats(stats)
                .connected(false)
                .build();
    }
}
