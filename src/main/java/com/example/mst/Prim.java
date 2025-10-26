package com.example.mst;

import java.util.*;

/**
 * Prim с min-кучей. decrease-key реализован повторной вставкой (ленивое удаление старых ключей).
 */
public final class Prim implements MSTAlgorithm {

    private static final class PQNode implements Comparable<PQNode> {
        final String v;
        final double key;

        PQNode(String v, double key) {
            this.v = v;
            this.key = key;
        }

        @Override
        public int compareTo(PQNode o) {
            return Double.compare(this.key, o.key);
        }
    }

    @Override
    public MSTResult compute(Graph graph) {
        long start = System.nanoTime();
        OperationStats stats = new OperationStats();

        int n = graph.vertexCount();
        List<Edge> mst = new ArrayList<>();
        boolean connected;

        if (n == 0) {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            return new MSTResult.Builder()
                    .algorithm(AlgorithmType.PRIM)
                    .mstEdges(mst)
                    .totalCost(0.0)
                    .originalVertexCount(0)
                    .originalEdgeCount(0)
                    .durationMillis(durationMs)
                    .stats(stats)
                    .connected(true)  // пустой граф считаем связным по определению
                    .build();
        }

        // choose arbitrary start
        String startV = graph.getVertices().iterator().next();

        Map<String, Double> bestKey = new HashMap<>();
        Map<String, Edge> parentEdge = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<PQNode> pq = new PriorityQueue<>();

        for (String v : graph.getVertices()) {
            bestKey.put(v, Double.POSITIVE_INFINITY);
            parentEdge.put(v, null);
        }
        bestKey.put(startV, 0.0);
        pq.offer(new PQNode(startV, 0.0));
        stats.incHeapOps();

        while (!pq.isEmpty()) {
            PQNode cur = pq.poll();
            stats.incHeapOps();
            if (visited.contains(cur.v)) continue;
            visited.add(cur.v);

            Edge pe = parentEdge.get(cur.v);
            if (pe != null) {
                mst.add(pe);
            }

            // просмотр смежных рёбер
            List<Edge> adj = graph.getAdjacency().get(cur.v);
            if (adj != null) {
                for (Edge e : adj) {
                    stats.incEdgeRelax();
                    String w = other(e, cur.v);
                    if (visited.contains(w)) continue;

                    // сравнение "новый ключ < лучший"
                    stats.incComparisons();
                    if (e.getWeight() < bestKey.get(w)) {
                        bestKey.put(w, e.getWeight());
                        parentEdge.put(w, e);
                        pq.offer(new PQNode(w, e.getWeight()));
                        stats.incHeapOps();
                    }
                }
            }
        }

        connected = (mst.size() == n - 1);
        double total = mst.stream().mapToDouble(Edge::getWeight).sum();

        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        return new MSTResult.Builder()
                .algorithm(AlgorithmType.PRIM)
                .mstEdges(mst)
                .totalCost(total)
                .originalVertexCount(graph.vertexCount())
                .originalEdgeCount(graph.edgeCount())
                .durationMillis(durationMs)
                .stats(stats)
                .connected(connected)
                .build();
    }

    private static String other(Edge e, String v) {
        if (e.getU().equals(v)) return e.getV();
        if (e.getV().equals(v)) return e.getU();
        throw new IllegalArgumentException("Edge is not incident to vertex " + v + ": " + e);
    }
}
