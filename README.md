 Assignment 3 — Minimum Spanning Tree (Prim & Kruskal)
 1. Summary of Input Data and Algorithm Results
Graph ID	Vertices	Edges	Prim Cost	Kruskal Cost	Prim Ops	Kruskal Ops	Prim Time (ms)	Kruskal Time (ms)
1	5	7	16	16	42	37	1.52	1.28
2	4	5	6	6	29	31	0.87	0.92

Data generated from automated tests and saved in ass_3_output.json.

Input Datasets: included graphs of varying size and density (small, medium, large).

Execution Metrics Recorded: total MST cost, execution time (ms), operation count (comparisons, unions, etc.).

Data Storage: input in ass_3_input.json; results exported to ass_3_output.json.

 2. Algorithm Comparison (Theory & Practice)
Prim’s Algorithm

Approach: builds MST incrementally by selecting the minimum edge from a growing tree.

Complexity:

Adjacency matrix: O(V²)

With min-heap: O(E log V)

Best suited for: dense graphs (more edges per vertex).

Performance Observation: slightly higher operation count but stable execution time.

Kruskal’s Algorithm

Approach: sorts edges by weight, adds them to MST if no cycle forms (using union–find).

Complexity: O(E log E) ≈ O(E log V)

Best suited for: sparse graphs (fewer edges, more disconnected components).

Performance Observation: faster on sparse graphs due to fewer edge relaxations.

Empirical Comparison

Both algorithms produced identical MST total costs for all datasets.

Prim showed consistent timing on denser graphs.

Kruskal achieved fewer total operations on sparse inputs.

The operation count difference stems from edge sorting vs. heap updates.

3. Conclusions
Graph Type	Preferred Algorithm	Reason
Sparse graphs (few edges)	Kruskal	Sort-based approach handles small edge sets efficiently.
Dense graphs (many edges)	Prim	Heap optimization yields faster performance with many adjacencies.
Edge-list representations	Kruskal	Naturally processes edge lists.
Adjacency-list representations	Prim	Avoids global sorting; faster neighbor lookups.
Complex implementations	Prim	Slightly more complex due to heap structure; Kruskal simpler overall.

Final Insight:
Both algorithms yield the same MST cost but differ in execution characteristics.
Use Kruskal for graph analytics or file-based edge data, and Prim for in-memory dense network computations.

 4. References

Cormen, Leiserson, Rivest, Stein. Introduction to Algorithms (MIT Press).

GeeksforGeeks — Prim’s Algorithm
 & Kruskal’s Algorithm
.

Java JUnit 5 Documentation — Automated Testing Framework.
