package com.example.mst;

import java.util.*;

public final class Graph {
    private final Set<String> vertices;
    private final List<Edge> edges;
    private final Map<String, List<Edge>> adj; // для Prim

    public Graph(Collection<String> vertices, Collection<Edge> edges) {
        if (vertices == null || edges == null) throw new IllegalArgumentException("Null args");
        this.vertices = Collections.unmodifiableSet(new LinkedHashSet<>(vertices));
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
        this.adj = buildAdj(this.vertices, this.edges);
        validateEdgesReferenceExistingVertices();
    }

    private static Map<String, List<Edge>> buildAdj(Set<String> vs, List<Edge> es) {
        Map<String, List<Edge>> map = new HashMap<>();
        for (String v : vs) map.put(v, new ArrayList<>());
        for (Edge e : es) {
            map.get(e.getU()).add(e);
            map.get(e.getV()).add(e);
        }
        return Collections.unmodifiableMap(map);
    }

    private void validateEdgesReferenceExistingVertices() {
        for (Edge e : edges) {
            if (!vertices.contains(e.getU()) || !vertices.contains(e.getV())) {
                throw new IllegalArgumentException("Edge references unknown vertex: " + e);
            }
        }
    }

    public Set<String> getVertices() { return vertices; }
    public List<Edge> getEdges() { return edges; }
    public Map<String, List<Edge>> getAdjacency() { return adj; }

    public int vertexCount() { return vertices.size(); }
    public int edgeCount() { return edges.size(); }

    public boolean isEmpty() { return vertices.isEmpty(); }
}
