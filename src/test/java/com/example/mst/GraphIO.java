package com.example.mst;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class GraphIO {

    public static final class InputEdge {
        String from, to;
        double weight;
    }

    public static final class InputGraph {
        long id;
        List<String> nodes;
        List<InputEdge> edges;
    }

    public static final class OutputAlgo {
        List<InputEdge> mst_edges;
        double total_cost;
        long operations_count;
        double execution_time_ms;
    }

    public static final class OutputItem {
        long graph_id;
        Stats input_stats;
        OutputAlgo prim;
        OutputAlgo kruskal;
    }
    public static final class Stats { int vertices; int edges; }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<InputGraph> readInput(File file) throws IOException {
        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            Type listType = new TypeToken<List<InputGraph>>(){}.getType();
            return gson.fromJson(root.getAsJsonArray("graphs"), listType);
        }
    }

    public static void writeOutput(File file, List<OutputItem> results) throws IOException {
        JsonObject root = new JsonObject();
        Type listType = new TypeToken<List<OutputItem>>(){}.getType();
        root.add("results", gson.toJsonTree(results, listType));
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(root, w);
        }
    }

    public static Graph toGraph(InputGraph ig) {
        List<Edge> edges = new ArrayList<>();
        for (InputEdge e : ig.edges) {
            edges.add(new Edge(e.from, e.to, e.weight));
        }
        return new Graph(ig.nodes, edges);
    }

    public static List<GraphIO.InputEdge> toIOEdges(List<Edge> es) {
        List<GraphIO.InputEdge> out = new ArrayList<>();
        for (Edge e : es) {
            GraphIO.InputEdge ie = new GraphIO.InputEdge();
            ie.from = e.getU();
            ie.to = e.getV();
            ie.weight = e.getWeight();
            out.add(ie);
        }
        return out;
    }
}
