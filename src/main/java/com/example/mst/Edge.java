package com.example.mst;

import java.util.Objects;

public final class Edge implements Comparable<Edge> {
    private final String u;
    private final String v;
    private final double weight;

    public Edge(String u, String v, double weight) {
        if (u == null || v == null) throw new IllegalArgumentException("Vertices cannot be null");
        if (u.equals(v)) throw new IllegalArgumentException("Self-loops are not allowed in MST input");
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    public String getU() { return u; }
    public String getV() { return v; }
    public double getWeight() { return weight; }

    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge e = (Edge) o;
        // неориентированный граф: {u,v} эквивалентно {v,u}
        boolean sameSet = (u.equals(e.u) && v.equals(e.v)) || (u.equals(e.v) && v.equals(e.u));
        return sameSet && Double.compare(weight, e.weight) == 0;
    }

    @Override
    public int hashCode() {
        // порядок вершин неважен
        int h = u.compareTo(v) <= 0 ? Objects.hash(u, v) : Objects.hash(v, u);
        return 31 * h + Double.hashCode(weight);
    }

    @Override
    public String toString() {
        return String.format("(%s - %s, w=%.3f)", u, v, weight);
    }
}
