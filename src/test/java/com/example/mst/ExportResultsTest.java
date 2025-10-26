package com.example.mst;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportResultsTest {

    @Test
    void export() throws Exception {
        var input = GraphIO.readInput(new File("ass_3_input.json"));

        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kruskal = new Kruskal();

        List<GraphIO.OutputItem> out = new ArrayList<>();

        for (var ig : input) {
            Graph g = GraphIO.toGraph(ig);

            long t0 = System.nanoTime();
            MSTResult rp = prim.compute(g);
            double primMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            MSTResult rk = kruskal.compute(g);
            double krMs = (System.nanoTime() - t0) / 1_000_000.0;

            GraphIO.OutputItem item = new GraphIO.OutputItem();
            item.graph_id = ig.id;

            item.input_stats = new GraphIO.Stats();
            item.input_stats.vertices = ig.nodes.size();
            item.input_stats.edges = ig.edges.size();

            item.prim = new GraphIO.OutputAlgo();
            item.prim.mst_edges = GraphIO.toIOEdges(rp.getMstEdges());
            item.prim.total_cost = rp.getTotalCost();
            item.prim.operations_count = rp.getStats().getComparisons()
                    + rp.getStats().getHeapOps()
                    + rp.getStats().getFinds()
                    + rp.getStats().getUnions()
                    + rp.getStats().getEdgeRelax();
            item.prim.execution_time_ms = primMs;

            item.kruskal = new GraphIO.OutputAlgo();
            item.kruskal.mst_edges = GraphIO.toIOEdges(rk.getMstEdges());
            item.kruskal.total_cost = rk.getTotalCost();
            item.kruskal.operations_count = rk.getStats().getComparisons()
                    + rk.getStats().getHeapOps()
                    + rk.getStats().getFinds()
                    + rk.getStats().getUnions()
                    + rk.getStats().getEdgeRelax();
            item.kruskal.execution_time_ms = krMs;

            out.add(item);
        }

        GraphIO.writeOutput(new File("ass_3_output.json"), out);
    }
}
