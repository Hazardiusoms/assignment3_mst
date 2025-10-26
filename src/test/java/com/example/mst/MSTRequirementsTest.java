package com.example.mst;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class MSTRequirementsTest {

    // ---------- Helpers

    private static boolean isAcyclicForest(List<String> nodes, List<Edge> edges) {
        // DSU (Union–Find) to ensure no cycles are present
        Map<String, String> parent = new HashMap<>();
        Map<String, Integer> rank = new HashMap<>();
        for (String v : nodes) { parent.put(v, v); rank.put(v, 0); }

        java.util.function.Function<String, String> find = new java.util.function.Function<String, String>() {
            @Override public String apply(String x) {
                String p = parent.get(x);
                if (!p.equals(x)) parent.put(x, this.apply(p));
                return parent.get(x);
            }
        };
        java.util.function.BiFunction<String, String, Boolean> union = (a, b) -> {
            String ra = find.apply(a), rb = find.apply(b);
            if (ra.equals(rb)) return false;
            int rA = rank.get(ra), rB = rank.get(rb);
            if (rA < rB) parent.put(ra, rb);
            else if (rA > rB) parent.put(rb, ra);
            else { parent.put(rb, ra); rank.put(ra, rA + 1); }
            return true;
        };

        for (Edge e : edges) {
            String u = e.getU(), v = e.getV();
            String ru = find.apply(u), rv = find.apply(v);
            if (ru.equals(rv)) return false; // cycle found
            union.apply(u, v);
        }
        return true;
    }

    private static Graph graphOf(String[] nodes, Object[][] undirectedEdges) {
        List<String> vs = Arrays.asList(nodes);
        List<Edge> es = new ArrayList<>();
        for (Object[] t : undirectedEdges) {
            es.add(new Edge((String) t[0], (String) t[1], ((Number) t[2]).doubleValue()));
        }
        return new Graph(vs, es);
    }

    private static long sumOps(OperationStats s) {
        return s.getComparisons() + s.getHeapOps() + s.getFinds() + s.getUnions() + s.getEdgeRelax();
    }

    private static void assertNonNegativeStats(OperationStats s) {
        assertAll(
                () -> assertTrue(s.getComparisons() >= 0, "comparisons < 0"),
                () -> assertTrue(s.getHeapOps() >= 0, "heapOps < 0"),
                () -> assertTrue(s.getFinds() >= 0, "finds < 0"),
                () -> assertTrue(s.getUnions() >= 0, "unions < 0"),
                () -> assertTrue(s.getEdgeRelax() >= 0, "edgeRelax < 0")
        );
    }

    private static void assertNonNegativeTiming(MSTResult r) {
        assertTrue(r.getDurationMillis() >= 0, "durationMillis is negative");
    }

    // ---------- A) Correctness on small hand-crafted graphs

    @Test
    void correctness_on_small_connected_graph() {
        // square with diagonals
        Graph g = graphOf(
                new String[]{"A","B","C","D"},
                new Object[][]{
                        {"A","B", 1}, {"B","C", 2}, {"C","D", 1}, {"D","A", 2},
                        {"A","C", 3}, {"B","D", 3}
                });

        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kruskal = new Kruskal();

        MSTResult rp = prim.compute(g);
        MSTResult rk = kruskal.compute(g);

        // total cost identical
        assertEquals(rp.getTotalCost(), rk.getTotalCost(), 1e-6);

        // |MST| = V - 1
        assertEquals(g.vertexCount() - 1, rp.getMstEdges().size());
        assertEquals(g.vertexCount() - 1, rk.getMstEdges().size());

        // acyclic
        List<String> nodes = new ArrayList<>(g.getVertices());
        assertTrue(isAcyclicForest(nodes, rp.getMstEdges()));
        assertTrue(isAcyclicForest(nodes, rk.getMstEdges()));

        // connects all vertices
        assertTrue(rp.isConnected());
        assertTrue(rk.isConnected());

        // metrics non-negative
        assertNonNegativeStats(rp.getStats());
        assertNonNegativeStats(rk.getStats());
        assertNonNegativeTiming(rp);
        assertNonNegativeTiming(rk);
    }

    @Test
    void disconnected_graph_is_handled_gracefully() {
        // Two separate edges (two components)
        Graph g = graphOf(
                new String[]{"A","B","C","D"},
                new Object[][]{
                        {"A","B", 1},
                        {"C","D", 1}
                });

        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kruskal = new Kruskal();

        MSTResult rp = prim.compute(g);
        MSTResult rk = kruskal.compute(g);

        // Both should report not connected
        assertFalse(rp.isConnected(), "Prim should mark graph as disconnected");
        assertFalse(rk.isConnected(), "Kruskal should mark graph as disconnected");

        // Forest (no cycles)
        List<String> nodes = Arrays.asList("A","B","C","D");
        assertTrue(isAcyclicForest(nodes, rp.getMstEdges()));
        assertTrue(isAcyclicForest(nodes, rk.getMstEdges()));

        // Metrics/time non-negative
        assertNonNegativeStats(rp.getStats());
        assertNonNegativeStats(rk.getStats());
        assertNonNegativeTiming(rp);
        assertNonNegativeTiming(rk);
    }

    // ---------- B) Dataset-based tests (run if ass_3_input.json exists)

    private static List<GraphIO.InputGraph> loadDatasetIfPresent() throws Exception {
        File f1 = new File("ass_3_input.json");                     // canonical name
        File f2 = new File("ass_3_input (1).json");                 // common alt name
        File src = f1.exists() ? f1 : (f2.exists() ? f2 : null);
        assumeTrue(src != null, "Dataset JSON not found in project root; skipping dataset tests.");
        return GraphIO.readInput(src);
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void dataset_correctness_cost_edges_cycles_connectivity() throws Exception {
        List<GraphIO.InputGraph> ds = loadDatasetIfPresent();
        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kr = new Kruskal();

        for (GraphIO.InputGraph ig : ds) {
            Graph g = GraphIO.toGraph(ig);

            MSTResult rp = prim.compute(g);
            MSTResult rk = kr.compute(g);

            // Total cost identical (allow tiny epsilon)
            assertEquals(rp.getTotalCost(), rk.getTotalCost(), 1e-6, "Total cost mismatch for graph " + ig.id);

            // Acyclic forests
            List<String> nodes = ig.nodes;
            assertTrue(isAcyclicForest(nodes, rp.getMstEdges()), "Prim produces a cycle for " + ig.id);
            assertTrue(isAcyclicForest(nodes, rk.getMstEdges()), "Kruskal produces a cycle for " + ig.id);

            if (rp.isConnected() && rk.isConnected()) {
                assertEquals(nodes.size() - 1, rp.getMstEdges().size(), "|MST|≠V-1 in Prim for " + ig.id);
                assertEquals(nodes.size() - 1, rk.getMstEdges().size(), "|MST|≠V-1 in Kruskal for " + ig.id);
            }

            // Timing and stats non-negative
            assertNonNegativeTiming(rp);
            assertNonNegativeTiming(rk);
            assertNonNegativeStats(rp.getStats());
            assertNonNegativeStats(rk.getStats());
        }
    }

    @Test
    void performance_fields_and_reproducibility_on_dataset() throws Exception {
        List<GraphIO.InputGraph> ds = loadDatasetIfPresent();
        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kr = new Kruskal();

        // First pass
        Map<Long, Double> primCost1 = new HashMap<>(), krCost1 = new HashMap<>();
        Map<Long, Integer> primEdges1 = new HashMap<>(), krEdges1 = new HashMap<>();
        Map<Long, Boolean> primConn1 = new HashMap<>(), krConn1 = new HashMap<>();

        for (GraphIO.InputGraph ig : ds) {
            Graph g = GraphIO.toGraph(ig);
            MSTResult rp = prim.compute(g);
            MSTResult rk = kr.compute(g);

            // Execution time reported in ms and non-negative
            assertTrue(rp.getDurationMillis() >= 0);
            assertTrue(rk.getDurationMillis() >= 0);

            // Operation counts non-negative and "consistent" (sum >= each component)
            long sp = sumOps(rp.getStats());
            long sk = sumOps(rk.getStats());
            assertTrue(sp >= rp.getStats().getComparisons());
            assertTrue(sp >= rp.getStats().getHeapOps());
            assertTrue(sp >= rp.getStats().getFinds());
            assertTrue(sp >= rp.getStats().getUnions());
            assertTrue(sp >= rp.getStats().getEdgeRelax());
            assertTrue(sk >= rk.getStats().getComparisons());
            assertTrue(sk >= rk.getStats().getHeapOps());
            assertTrue(sk >= rk.getStats().getFinds());
            assertTrue(sk >= rk.getStats().getUnions());
            assertTrue(sk >= rk.getStats().getEdgeRelax());

            primCost1.put(ig.id, rp.getTotalCost());
            krCost1.put(ig.id, rk.getTotalCost());
            primEdges1.put(ig.id, rp.getMstEdges().size());
            krEdges1.put(ig.id, rk.getMstEdges().size());
            primConn1.put(ig.id, rp.isConnected());
            krConn1.put(ig.id, rk.isConnected());
        }

        // Second pass (reproducibility: same dataset => same results)
        for (GraphIO.InputGraph ig : ds) {
            Graph g = GraphIO.toGraph(ig);
            MSTResult rp = prim.compute(g);
            MSTResult rk = kr.compute(g);

            assertEquals(primCost1.get(ig.id), rp.getTotalCost(), 1e-9, "Prim total cost not reproducible for " + ig.id);
            assertEquals(krCost1.get(ig.id), rk.getTotalCost(), 1e-9, "Kruskal total cost not reproducible for " + ig.id);
            assertEquals(primEdges1.get(ig.id).intValue(), rp.getMstEdges().size(), "Prim edge count not reproducible");
            assertEquals(krEdges1.get(ig.id).intValue(), rk.getMstEdges().size(), "Kruskal edge count not reproducible");
            assertEquals(primConn1.get(ig.id), rp.isConnected(), "Prim connectivity flag not reproducible");
            assertEquals(krConn1.get(ig.id), rk.isConnected(), "Kruskal connectivity flag not reproducible");
        }
    }

    // ---------- C) Quick sanity check of time budgets per graph size (soft)

    @Test
    void soft_time_budgets_per_size_if_dataset_available() throws Exception {
        List<GraphIO.InputGraph> ds = loadDatasetIfPresent();
        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kr = new Kruskal();

        for (GraphIO.InputGraph ig : ds) {
            int n = ig.nodes.size();
            long budgetMs =
                    (n <= 50)   ? 50  :
                            (n <= 400)  ? 250 :
                                    (n <= 1100) ? 1500 : 4000;

            assertTimeoutPreemptively(Duration.ofMillis(budgetMs), () -> {
                Graph g = GraphIO.toGraph(ig);
                prim.compute(g);
                kr.compute(g);
            }, "Soft budget exceeded for graph " + ig.id + " with n=" + n);
        }
    }
}
