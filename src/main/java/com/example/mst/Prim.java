package com.example.mst;

public final class Prim implements MSTAlgorithm {

    @Override
    public MSTResult compute(Graph graph) {
        long start = System.nanoTime();
        OperationStats stats = new OperationStats();

        // TODO: Реализация Prim с мин-кучей (PriorityQueue) и decrease-key (через повторную вставку)
        // Учитывать:
        //  - stats.incHeapOps(), stats.incComparisons(), stats.incEdgeRelax()
        //  - корректно отметить connected=true только если собрано |V|-1 рёбер

        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        return new MSTResult.Builder()
                .algorithm(AlgorithmType.PRIM)
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
