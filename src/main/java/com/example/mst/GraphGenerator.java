package com.example.mst;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Генерирует 5 small (≈30), 10 medium (≈300), 10 large (≈1000),
 * 3 extra-large (1300, 1600, 2000) связных графов.
 *
 * Плотность задаётся диапазонами средней степени (min-max) через CLI.
 * Для каждой группы выбирается случайная средняя степень из указанного диапазона.
 *
 * Пример:
 *   mvn -q -DskipTests package
 *   java -cp target/mst-assignment3-1.0-SNAPSHOT.jar com.example.mst.GraphGenerator \
 *        --out ass_3_input.json --seed 42 \
 *        --deg-small 4-6 --deg-medium 6-10 --deg-large 8-12 --deg-xl 8-12
 *
 * Формат JSON: { "graphs": [ {id, nodes[], edges[]}, ... ] }
 */
public final class GraphGenerator {

    // --- Диапазоны по умолчанию (подходят для скоростных автотестов на Java 17)
    private static Range DEG_SMALL = new Range(4, 6);   // n≈30
    private static Range DEG_MED  = new Range(6, 10);  // n≈300
    private static Range DEG_LRG  = new Range(8, 12);  // n≈1000
    private static Range DEG_XL   = new Range(8, 12);  // n>=1300

    // веса рёбер ~ U[1.0, 100.0)
    private static final double W_MIN = 1.0;
    private static final double W_MAX = 100.0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {
        String outPath = "ass_3_input.json";
        Long seed = 2025_10_26L;

        // --- CLI
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--out":       outPath = args[++i]; break;
                case "--seed":      seed = Long.parseLong(args[++i]); break;
                case "--deg-small": DEG_SMALL = parseRange(args[++i]); break;
                case "--deg-medium":DEG_MED  = parseRange(args[++i]); break;
                case "--deg-large": DEG_LRG  = parseRange(args[++i]); break;
                case "--deg-xl":    DEG_XL   = parseRange(args[++i]); break;
                default:
                    System.err.println("Unknown arg: " + args[i]);
            }
        }
        validateRanges();

        Random rnd = new Random(seed);

        // --- План генерации
        List<Spec> plan = new ArrayList<>();
        for (int i = 0; i < 5;  i++) plan.add(new Spec(Size.SMALL, 30));
        for (int i = 0; i < 10; i++) plan.add(new Spec(Size.MEDIUM, 300));
        for (int i = 0; i < 10; i++) plan.add(new Spec(Size.LARGE, 1000));
        plan.add(new Spec(Size.XL, 1300));
        plan.add(new Spec(Size.XL, 1600));
        plan.add(new Spec(Size.XL, 2000));

        JsonArray graphs = new JsonArray();
        long nextId = 1;

        for (Spec s : plan) {
            int n = s.n;
            int avgDeg = pickAvgDegree(s.size, rnd);
            int targetM = Math.min((n * avgDeg) / 2, n * (n - 1) / 2);

            GraphData g = randomConnectedGraph(n, targetM, rnd);

            JsonObject gJson = new JsonObject();
            gJson.addProperty("id", nextId++);

            JsonArray nodes = new JsonArray();
            for (int i = 0; i < n; i++) nodes.add("v" + i);
            gJson.add("nodes", nodes);

            JsonArray edges = new JsonArray();
            for (EdgeRec e : g.edges) {
                JsonObject ej = new JsonObject();
                ej.addProperty("from", "v" + e.u);
                ej.addProperty("to", "v" + e.v);
                ej.addProperty("weight", e.w);
                edges.add(ej);
            }
            gJson.add("edges", edges);

            graphs.add(gJson);
        }

        JsonObject root = new JsonObject();
        root.add("graphs", graphs);

        File out = new File(outPath);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            GSON.toJson(root, w);
        }
        System.out.println("Wrote: " + out.getAbsolutePath() + " (seed=" + seed + ")");
        System.out.printf("Used degree ranges: small=%s, medium=%s, large=%s, xl=%s%n",
                DEG_SMALL, DEG_MED, DEG_LRG, DEG_XL);
    }

    private static void validateRanges() {
        if (!DEG_SMALL.valid() || !DEG_MED.valid() || !DEG_LRG.valid() || !DEG_XL.valid()) {
            throw new IllegalArgumentException("Degree ranges must satisfy 1 <= min <= max");
        }
    }

    private static int pickAvgDegree(Size size, Random rnd) {
        Range r = (size == Size.SMALL) ? DEG_SMALL :
                (size == Size.MEDIUM) ? DEG_MED :
                        (size == Size.LARGE) ? DEG_LRG : DEG_XL;
        return uniformInt(rnd, r.min, r.max);
    }

    /**
     * Связный неориентированный граф.
     * Шаг 1: строим случайное дерево (обеспечивает связность).
     * Шаг 2: добавляем случайные рёбра до целевого числа без петель и дублей.
     */
    private static GraphData randomConnectedGraph(int n, int targetM, Random rnd) {
        HashSet<Long> used = new HashSet<>(Math.max(targetM * 2, 16));
        ArrayList<EdgeRec> edges = new ArrayList<>(Math.max(targetM, n - 1));

        // 1) Случайное остовное дерево
        int[] order = randomPermutation(n, rnd);
        for (int i = 1; i < n; i++) {
            int u = order[i];
            int v = order[rnd.nextInt(i)];
            addEdgeIfNew(u, v, n, used, edges, rnd);
        }

        // 2) Досыпаем рёбра до targetM
        int maxPossible = n * (n - 1) / 2;
        int target = Math.min(targetM, maxPossible);
        while (edges.size() < target) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) continue;
            addEdgeIfNew(u, v, n, used, edges, rnd);
        }
        return new GraphData(n, edges);
    }

    private static void addEdgeIfNew(int a, int b, int n, Set<Long> used, List<EdgeRec> out, Random rnd) {
        int u = Math.min(a, b), v = Math.max(a, b);
        long key = ((long) u) * n + v;
        if (used.add(key)) {
            double w = W_MIN + (W_MAX - W_MIN) * rnd.nextDouble();
            w = Math.round(w * 1000.0) / 1000.0; // до 3 знаков
            out.add(new EdgeRec(u, v, w));
        }
    }

    private static int[] randomPermutation(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
        return a;
    }

    private static int uniformInt(Random rnd, int lo, int hiInclusive) {
        return lo + rnd.nextInt(hiInclusive - lo + 1);
    }

    // --- helpers & types

    private enum Size { SMALL, MEDIUM, LARGE, XL }

    private static final class Spec {
        final Size size; final int n;
        Spec(Size s, int n) { this.size = s; this.n = n; }
    }

    private static final class EdgeRec {
        final int u, v; final double w;
        EdgeRec(int u, int v, double w) { this.u = u; this.v = v; this.w = w; }
    }

    private static final class GraphData {
        final int n; final List<EdgeRec> edges;
        GraphData(int n, List<EdgeRec> edges) { this.n = n; this.edges = edges; }
    }

    private static final class Range {
        final int min, max;
        Range(int min, int max) { this.min = min; this.max = max; }
        boolean valid() { return min >= 1 && min <= max; }
        @Override public String toString() { return min + "-" + max; }
    }

    private static Range parseRange(String s) {
        // Форматы: "8-12" или "9"
        if (s.contains("-")) {
            String[] p = s.split("-");
            if (p.length != 2) throw new IllegalArgumentException("Bad range: " + s);
            return new Range(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()));
        }
        int v = Integer.parseInt(s.trim());
        return new Range(v, v);
    }
}
