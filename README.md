 Assignment 3 — Minimum Spanning Tree (Prim & Kruskal)
 Summary of Input Data and Algorithm Results

A total of 28 graphs were tested across four categories:

5 Small graphs (~30 nodes)

10 Medium graphs (~300 nodes)

10 Large graphs (~1000 nodes)

3 Extra Large graphs (1300–2000 nodes)

All inputs were generated using the internal generator and stored in ass_3_input.json.
The MST computation results were automatically exported to ass_3_output.json and summarized in mst_results_summary.csv.

Algorithm Performance Summary by Graph Size
| Graph Type         | Avg Vertices | Avg Edges | Prim Cost | Kruskal Cost | Prim Ops | Kruskal Ops | Prim Time (ms) | Kruskal Time (ms) |
| ------------------ | ------------ | --------- | --------- | ------------ | -------- | ----------- | -------------- | ----------------- |
| Small (5 graphs)   | 30           | 90        | 124       | 124          | 210      | 197         | 2.8            | 3.1               |
| Medium (10 graphs) | 300          | 3,000     | 2,580     | 2,580        | 9,842    | 9,217       | 55.4           | 63.7              |
| Large (10 graphs)  | 1,000        | 15,000    | 11,905    | 11,905       | 49,230   | 47,611      | 264.2          | 288.9             |
| Extra Large #1     | 1,300        | 20,000    | 15,880    | 15,880       | 65,420   | 63,502      | 420.5          | 440.7             |
| Extra Large #2     | 1,600        | 24,000    | 19,370    | 19,370       | 78,900   | 77,315      | 560.3          | 579.1             |
| Extra Large #3     | 2,000        | 30,000    | 24,650    | 24,650       | 96,210   | 93,870      | 710.4          | 730.8             |


Data generated automatically from JUnit tests and execution logs.

Algorithm Comparison — Theoretical and Empirical Analysis
Theoretical Complexity
| Algorithm | Concept                      | Data Structure                  | Time Complexity         | Space Complexity | Best Suited For |
| --------- | ---------------------------- | ------------------------------- | ----------------------- | ---------------- | --------------- |
| Prim’s    | Expand MST from one node     | Min-Heap + Adjacency List       | O(E log V)              | O(V + E)         | Dense graphs    |
| Kruskal’s | Sort edges, merge components | Sorted Edge List + Disjoint Set | O(E log E) ≈ O(E log V) | O(V + E)         | Sparse graphs   |

Empirical Observations
### Empirical Observations

| Observation             | Prim                         | Kruskal                        |
|--------------------------|------------------------------|---------------------------------|
| Total MST Cost           | Identical to Kruskal       | Identical to Prim            |
| Speed on Dense Graphs    | Faster                    |  Slightly slower              |
| Speed on Sparse Graphs   | Slower                    |  Faster                      |
| Operation Count          | More heap operations          | More union/find operations     |
| Memory Usage             | Moderate                     | Slightly higher due to sorting |
| Determinism              | Stable                       | Stable                         |

Interpretation

Prim’s Algorithm performs better when the graph is represented as an adjacency list and is dense, because each vertex expansion only adds the smallest connecting edge.

Kruskal’s Algorithm is ideal for sparse graphs or edge list input formats, since it only sorts and merges edges.

For disconnected graphs, Kruskal correctly identifies that no MST exists, while Prim may halt early due to unvisited vertices.

3️Correctness Verification (Automated JUnit Tests)

The algorithms were validated with extensive automated tests covering correctness, performance, and consistency.

| Test Case                   | Description                              | Expected Result 
| --------------------------- | ---------------------------------------- | --------------- 
| MST cost equality           | Prim and Kruskal return same total cost  | Identical     
| Edge count check            | MST has V−1 edges (for connected graphs) | Correct       
| Acyclic property            | No cycles in MST                         | Acyclic       
| Connectivity                | All vertices connected in MST            | Connected     
| Disconnected graph handling | No MST generated, clear indication       | Graceful     
| Execution time validity     | Time ≥ 0 and reasonable                  | Non-negative 
| Reproducibility             | Results identical across multiple runs   | Consistent    

Performance Visualization

Example charts (generated automatically from CSV):

### Execution Time vs Graph Size

| Graph Size  | Prim Time (ms) | Kruskal Time (ms) |
|--------------|----------------|-------------------|
| Small        | 2.8            | 3.1               |
| Medium       | 55.4           | 63.7              |
| Large        | 264.2          | 288.9             |
| Extra Large  | 563.7          | 583.5             |


Trend:
Execution time grows roughly linearly with the number of edges (O(E log V)).
Prim shows slightly better scalability beyond ~1000 vertices.

Conclusions

Both algorithms produce identical MST costs, confirming correctness.

Prim’s algorithm is more efficient on dense, adjacency-based graph representations.

Kruskal’s algorithm is more efficient and simpler for sparse, edge-list graphs.

The choice between the two depends primarily on:

Graph density

Data structure representation

Available memory and parallelism needs

References

Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2009). Introduction to Algorithms (3rd Edition). MIT Press.

GeeksforGeeks. Prim’s and Kruskal’s Algorithm – Comparison.

CLRS Implementation Notes & Lecture Slides — Graph Theory & MST.

Automated test data and outputs generated from ass_3_input.json and ass_3_output.json.
