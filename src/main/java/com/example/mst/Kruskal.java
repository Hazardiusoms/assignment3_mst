package com.example.mst;

import java.util.*;

/**
 * Kruskal с учётом сравнений в сортировке (через Comparator, инкрементирующий счётчик).
 */
public final class Kruskal implements MSTAlgorithm {

    @Override
    public MSTResult compute(Graph graph) {
        long start = System.nanoTime();
        OperationStats stats = new OperationStats();

        int n = graph.vertexCount();
        List<Edge> mst = new ArrayList<>();
        double total = 0.0;

        if (n == 0) {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            return new MSTResult.Builder()
                    .algorithm(AlgorithmType.KRUSKAL)
                    .mstEdges(mst)
                    .totalCost(0.0)
                    .originalVertexCount(0)
                    .originalEdgeCount(0)
                    .durationMillis(durationMs)
                    .stats(stats)
                    .connected(true)
                    .build();
        }

        // 1) сортировка рёбер по весу с учётом числа сравнений
        List<Edge> edges = new ArrayList<>(graph.getEdges());
        edges.sort((a, b) -> {
            stats.incComparisons();
            return Double.compare(a.getWeight(), b.getWeight());
        });

        // 2) DSU
        DisjointSet dsu = new DisjointSet(graph.getVertices(), stats);

        // 3) Проход по рёбрам
        for (Edge e : edges) {
            String u = e.getU();
            String v = e.getV();

            // find/find посчитаются внутри dsu
            if (dsu.union(u, v)) {
                mst.add(e);
                total += e.getWeight();
                if (mst.size() == n - 1) break;
            }
        }

        boolean connected = (mst.size() == n - 1);

        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        return new MSTResult.Builder()
                .algorithm(AlgorithmType.KRUSKAL)
                .mstEdges(mst)
                .totalCost(total)
                .originalVertexCount(graph.vertexCount())
                .originalEdgeCount(graph.edgeCount())
                .durationMillis(durationMs)
                .stats(stats)
                .connected(connected)
                .build();
    }
}
