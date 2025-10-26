package com.example.mst;

import java.util.HashMap;
import java.util.Map;

final class DisjointSet {
    private final Map<String, String> parent = new HashMap<>();
    private final Map<String, Integer> rank = new HashMap<>();
    private final OperationStats stats;

    DisjointSet(Iterable<String> vertices, OperationStats stats) {
        this.stats = stats;
        for (String v : vertices) {
            parent.put(v, v);
            rank.put(v, 0);
        }
    }

    String find(String x) {
        // считаем один вызов find
        stats.incFinds();
        String p = parent.get(x);
        if (!p.equals(x)) {
            parent.put(x, find(p)); // path compression
        }
        return parent.get(x);
    }

    boolean union(String a, String b) {
        String ra = find(a);
        String rb = find(b);
        if (ra.equals(rb)) return false;

        // union by rank
        int rA = rank.get(ra);
        int rB = rank.get(rb);

        if (rA < rB) {
            parent.put(ra, rb);
        } else if (rA > rB) {
            parent.put(rb, ra);
        } else {
            parent.put(rb, ra);
            rank.put(ra, rA + 1);
        }
        stats.incUnions();
        return true;
    }
}
