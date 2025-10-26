package com.example.mst;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class CsvExporter {

    private CsvExporter() {}

    /** bucket by |V|: small/medium/large/extra-large (как в тестах) */
    public static String bucketFor(int n) {
        if (n <= 50)   return "small";
        if (n <= 400)  return "medium";
        if (n <= 1100) return "large";
        return "extra-large";
    }

    /** Подробная выгрузка: строка на граф. */
    public static void writeDetailedCSV(List<GraphIO.OutputItem> results, File file) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            // header
            w.write(String.join(",",
                    "graph_id","bucket","vertices","edges",
                    "prim_cost","kruskal_cost","cost_match",
                    "prim_time_ms","kruskal_time_ms",
                    "prim_ops","kruskal_ops","prim_vs_kr_speed"));
            w.write("\n");

            for (GraphIO.OutputItem it : results) {
                int v = it.input_stats.vertices;
                int e = it.input_stats.edges;
                String bucket = bucketFor(v);

                Double primCost = it.prim != null ? it.prim.total_cost : null;
                Double krCost   = it.kruskal != null ? it.kruskal.total_cost : null;
                Boolean costMatch = (primCost != null && krCost != null)
                        ? Math.abs(primCost - krCost) <= 1e-6
                        : null;

                Double primMs = it.prim != null ? it.prim.execution_time_ms : null;
                Double krMs   = it.kruskal != null ? it.kruskal.execution_time_ms : null;
                Long primOps  = it.prim != null ? it.prim.operations_count : null;
                Long krOps    = it.kruskal != null ? it.kruskal.operations_count : null;

                Double speed = (primMs != null && krMs != null && krMs != 0.0)
                        ? primMs / krMs : null;

                // CSV row (null -> empty)
                w.write(joinCsv(
                        it.graph_id,
                        bucket,
                        v, e,
                        primCost, krCost,
                        costMatch,
                        primMs, krMs,
                        primOps, krOps,
                        speed));
                w.write("\n");
            }
        }
    }

    /** Сводная выгрузка по классам размеров. */
    public static void writeSummaryCSV(List<GraphIO.OutputItem> results, File file) throws IOException {
        // сгруппируем
        Map<String, List<GraphIO.OutputItem>> byBucket = results.stream().collect(
                Collectors.groupingBy(it -> bucketFor(it.input_stats.vertices),
                        LinkedHashMap::new, Collectors.toList()));

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(String.join(",",
                    "bucket","graphs",
                    "avg_vertices","avg_edges",
                    "prim_time_ms_avg","kruskal_time_ms_avg",
                    "prim_ops_avg","kruskal_ops_avg",
                    "cost_match_rate","prim_faster_share"));
            w.write("\n");

            for (Map.Entry<String, List<GraphIO.OutputItem>> e : byBucket.entrySet()) {
                String bucket = e.getKey();
                List<GraphIO.OutputItem> group = e.getValue();

                int graphs = group.size();
                double avgV = mean(group.stream().map(it -> (double) it.input_stats.vertices));
                double avgE = mean(group.stream().map(it -> (double) it.input_stats.edges));
                double primAvgMs = mean(group.stream().map(it -> it.prim != null ? it.prim.execution_time_ms : null));
                double krAvgMs   = mean(group.stream().map(it -> it.kruskal != null ? it.kruskal.execution_time_ms : null));
                double primOps   = mean(group.stream().map(it -> it.prim != null ? (double) it.prim.operations_count : null));
                double krOps     = mean(group.stream().map(it -> it.kruskal != null ? (double) it.kruskal.operations_count : null));

                double matchRate = mean(group.stream().map(it -> {
                    if (it.prim == null || it.kruskal == null) return null;
                    Double a = it.prim.total_cost, b = it.kruskal.total_cost;
                    if (a == null || b == null) return null;
                    return Math.abs(a - b) <= 1e-6 ? 1.0 : 0.0;
                }));

                double primFasterShare = mean(group.stream().map(it -> {
                    if (it.prim == null || it.kruskal == null) return null;
                    Double a = it.prim.execution_time_ms, b = it.kruskal.execution_time_ms;
                    if (a == null || b == null || b == 0.0) return null;
                    return a < b ? 1.0 : 0.0;
                }));

                w.write(joinCsv(bucket, graphs, avgV, avgE, primAvgMs, krAvgMs, primOps, krOps, matchRate, primFasterShare));
                w.write("\n");
            }
        }
    }

    // --- helpers

    private static String joinCsv(Object... vals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(Objects.toString(vals[i], "")));
        }
        return sb.toString();
    }

    private static String escapeCsv(String s) {
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needQuotes) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static double mean(java.util.stream.Stream<Double> stream) {
        double sum = 0.0; int cnt = 0;
        for (Double d : (Iterable<Double>) stream.filter(Objects::nonNull)::iterator) {
            sum += d; cnt++;
        }
        return cnt == 0 ? Double.NaN : (sum / cnt);
    }
}
