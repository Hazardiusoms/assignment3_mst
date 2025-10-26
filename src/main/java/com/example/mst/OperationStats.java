package com.example.mst;

public final class OperationStats {
    // Счётчики "ключевых действий" для требований задания
    private long comparisons;   // сравнения ключей/весов
    private long heapOps;       // операции с очередью (push/pop/decreaseKey)
    private long unions;        // объединения в DSU (для Kruskal)
    private long finds;         // операции find в DSU
    private long edgeRelax;     // "рассмотрено" ребёр / релаксаций

    public void incComparisons() { comparisons++; }
    public void addComparisons(long k) { comparisons += k; }

    public void incHeapOps() { heapOps++; }
    public void addHeapOps(long k) { heapOps += k; }

    public void incUnions() { unions++; }
    public void addUnions(long k) { unions += k; }

    public void incFinds() { finds++; }
    public void addFinds(long k) { finds += k; }

    public void incEdgeRelax() { edgeRelax++; }
    public void addEdgeRelax(long k) { edgeRelax += k; }

    public long getComparisons() { return comparisons; }
    public long getHeapOps() { return heapOps; }
    public long getUnions() { return unions; }
    public long getFinds() { return finds; }
    public long getEdgeRelax() { return edgeRelax; }
}
