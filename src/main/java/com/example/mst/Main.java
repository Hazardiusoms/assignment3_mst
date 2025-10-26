package com.example.mst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        try {
            String inputPath = "ass_3_input.json";
            String outputPath = "ass_3_output.json";
            String csvDetailed = "mst_results_detailed.csv";
            String csvSummary  = "mst_results_summary.csv";

            // CLI: --in --out --csv-detailed --csv-summary
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--in":  inputPath  = args[++i]; break;
                    case "--out": outputPath = args[++i]; break;
                    case "--csv-detailed": csvDetailed = args[++i]; break;
                    case "--csv-summary":  csvSummary  = args[++i]; break;
                    default: /* ignore unknown */ ;
                }
            }

            System.out.printf("Loading dataset: %s%n", inputPath);
            List<GraphIO.InputGraph> dataset = GraphIO.readInput(new File(inputPath));
            System.out.printf("Loaded %d graphs%n", dataset.size());

            MSTAlgorithm prim = new Prim();
            MSTAlgorithm kruskal = new Kruskal();

            List<GraphIO.OutputItem> results = new ArrayList<>();
            int count = 0;

            for (GraphIO.InputGraph ig : dataset) {
                count++;
                Graph g = GraphIO.toGraph(ig);

                System.out.printf("Graph #%d (id=%d): |V|=%d, |E|=%d ...%n",
                        count, ig.id, ig.nodes.size(), ig.edges.size());

                long t0 = System.nanoTime();
                MSTResult rp = prim.compute(g);
                double primMs = (System.nanoTime() - t0) / 1_000_000.0;

                t0 = System.nanoTime();
                MSTResult rk = kruskal.compute(g);
                double krMs = (System.nanoTime() - t0) / 1_000_000.0;

                System.out.printf("  Prim   : cost=%.3f, edges=%d, time=%.2f ms%n",
                        rp.getTotalCost(), rp.getMstEdges().size(), primMs);
                System.out.printf("  Kruskal: cost=%.3f, edges=%d, time=%.2f ms%n",
                        rk.getTotalCost(), rk.getMstEdges().size(), krMs);

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

                results.add(item);
            }

            // JSON
            GraphIO.writeOutput(new File(outputPath), results);
            System.out.printf("%nJSON saved to: %s%n", outputPath);

            // CSV (автоматически)
            CsvExporter.writeDetailedCSV(results, new File(csvDetailed));
            CsvExporter.writeSummaryCSV(results, new File(csvSummary));
            System.out.printf("CSV saved: %s, %s%n", csvDetailed, csvSummary);

            System.out.println("\nAll graphs processed successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Error while running MST computation: " + ex.getMessage());
            System.exit(1);
        }
    }
}
