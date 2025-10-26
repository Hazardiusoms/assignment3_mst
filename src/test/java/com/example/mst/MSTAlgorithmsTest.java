package com.example.mst;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MSTAlgorithmsTest {

    static List<GraphIO.InputGraph> dataset;

    @BeforeAll
    static void load() throws Exception {
        // путь подправь под свой проект/ресурсы
        File f = new File("ass_3_input.json");
        dataset = GraphIO.readInput(f);
        assertNotNull(dataset);
        assertFalse(dataset.isEmpty(), "Input graphs must not be empty");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void prim_equals_kruskal_on_total_cost_and_forest_property() {
        MSTAlgorithm prim = new Prim();
        MSTAlgorithm kr = new Kruskal();

        for (GraphIO.InputGraph ig : dataset) {
            Graph g = GraphIO.toGraph(ig);

            MSTResult rp = prim.compute(g);
            MSTResult rk = kr.compute(g);

            // сумма весов совпадает
            assertEquals(rp.getTotalCost(), rk.getTotalCost(), 1e-6, "Total cost mismatch for graph " + ig.id);

            // лес без циклов
            assertTrue(isAcyclicForest(ig.nodes, rp.getMstEdges()), "Prim MST not a forest for " + ig.id);
            assertTrue(isAcyclicForest(ig.nodes, rk.getMstEdges()), "Kruskal MST not a forest for " + ig.id);

            if (rp.isConnected() && rk.isConnected()) {
                assertEquals(ig.nodes.size() - 1, rp.getMstEdges().size(), "Prim |MST| invalid for " + ig.id);
                assertEquals(ig.nodes.size() - 1, rk.getMstEdges().size(), "Kruskal |MST| invalid for " + ig.id);
            }
            // базовые метрики
            assertTrue(rp.getStats().getComparisons() >= 0 && rk.getStats().getComparisons() >= 0);
            assertTrue(rp.getDurationMillis() >= 0 && rk.getDurationMillis() >= 0);
        }
    }

    @Test
    void soft_time_budgets_per_size() {
        for (GraphIO.InputGraph ig : dataset) {
            int n = ig.nodes.size();
            long budgetMs =
                    (n <= 50) ? 50 :
                            (n <= 400) ? 250 :
                                    (n <= 1100) ? 1500 :
                                            4000;

            assertTimeoutPreemptively(Duration.ofMillis(budgetMs), () -> {
                MSTAlgorithm prim = new Prim();
                MSTAlgorithm kr = new Kruskal();
                Graph g = GraphIO.toGraph(ig);
                prim.compute(g);
                kr.compute(g);
            }, "Time budget exceeded for graph " + ig.id + " with n=" + n);
        }
    }

    private static boolean isAcyclicForest(List<String> nodes, List<Edge> edges) {
        // DSU-проверка
        Map<String, String> parent = new HashMap<>();
        Map<String, Integer> rank = new HashMap<>();
        for (String v : nodes) { parent.put(v, v); rank.put(v,0); }

        java.util.function.Function<String,String> find = new java.util.function.Function<String,String>() {
            @Override public String apply(String x) { String p = parent.get(x); return p.equals(x) ? p : parent.put(x, this.apply(p)); }
        };
        java.util.function.BiFunction<String,String,Boolean> union = (a,b) -> {
            String ra = find.apply(a), rb = find.apply(b);
            if (ra.equals(rb)) return false;
            int rA = rank.get(ra), rB = rank.get(rb);
            if (rA < rB) parent.put(ra, rb);
            else if (rA > rB) parent.put(rb, ra);
            else { parent.put(rb, ra); rank.put(ra, rA+1); }
            return true;
        };

        for (Edge e : edges) {
            String u = e.getU(), v = e.getV();
            String ru = find.apply(u), rv = find.apply(v);
            if (ru.equals(rv)) return false; // цикл
            union.apply(u, v);
        }
        return true;
    }
}
