package com.example.mst;

import java.util.*;

public final class MSTResult {
    private final AlgorithmType algorithm;
    private final List<Edge> mstEdges;       // |V|-1 ребро
    private final double totalCost;
    private final int originalVertexCount;
    private final int originalEdgeCount;
    private final long durationMillis;
    private final OperationStats stats;
    private final boolean connected;         // удалось ли покрыть все вершины

    private MSTResult(Builder b) {
        this.algorithm = b.algorithm;
        this.mstEdges = Collections.unmodifiableList(new ArrayList<>(b.mstEdges));
        this.totalCost = b.totalCost;
        this.originalVertexCount = b.originalVertexCount;
        this.originalEdgeCount = b.originalEdgeCount;
        this.durationMillis = b.durationMillis;
        this.stats = b.stats;
        this.connected = b.connected;
    }

    public AlgorithmType getAlgorithm() { return algorithm; }
    public List<Edge> getMstEdges() { return mstEdges; }
    public double getTotalCost() { return totalCost; }
    public int getOriginalVertexCount() { return originalVertexCount; }
    public int getOriginalEdgeCount() { return originalEdgeCount; }
    public long getDurationMillis() { return durationMillis; }
    public OperationStats getStats() { return stats; }
    public boolean isConnected() { return connected; }

    public static class Builder {
        private AlgorithmType algorithm;
        private List<Edge> mstEdges = new ArrayList<>();
        private double totalCost;
        private int originalVertexCount;
        private int originalEdgeCount;
        private long durationMillis;
        private OperationStats stats = new OperationStats();
        private boolean connected;

        public Builder algorithm(AlgorithmType a) { this.algorithm = a; return this; }
        public Builder mstEdges(Collection<Edge> es) { this.mstEdges = new ArrayList<>(es); return this; }
        public Builder totalCost(double c) { this.totalCost = c; return this; }
        public Builder originalVertexCount(int v) { this.originalVertexCount = v; return this; }
        public Builder originalEdgeCount(int e) { this.originalEdgeCount = e; return this; }
        public Builder durationMillis(long ms) { this.durationMillis = ms; return this; }
        public Builder stats(OperationStats s) { this.stats = s; return this; }
        public Builder connected(boolean c) { this.connected = c; return this; }
        public MSTResult build() { return new MSTResult(this); }
    }
}
