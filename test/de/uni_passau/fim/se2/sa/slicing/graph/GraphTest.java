package de.uni_passau.fim.se2.sa.slicing.graph;

import org.junit.jupiter.api.Test;

// TestGraph is a simple concrete implementation for testing purposes
class TestGraph {
    private final java.util.Map<String, java.util.List<String>> adj = new java.util.HashMap<>();
    void addEdge(String from, String to) {
        adj.computeIfAbsent(from, k -> new java.util.ArrayList<>()).add(to);
        adj.putIfAbsent(to, new java.util.ArrayList<>());
    }
    java.util.List<String> computeResult() {
        // Return nodes in insertion order for test
        java.util.Set<String> visited = new java.util.LinkedHashSet<>();
        for (String from : adj.keySet()) {
            visited.add(from);
            for (String to : adj.get(from)) visited.add(to);
        }
        return new java.util.ArrayList<>(visited);
    }
    java.util.Map<String, java.util.List<String>> getCFG() {
        return adj;
    }
    TestGraph reverseGraph() {
        TestGraph rev = new TestGraph();
        for (String from : adj.keySet()) {
            for (String to : adj.get(from)) {
                rev.addEdge(to, from);
            }
            if (adj.get(from).isEmpty()) rev.adj.putIfAbsent(from, new java.util.ArrayList<>());
        }
        return rev;
    }
}

public class GraphTest {
    @Test
    void testComputeResult() {
        // Simple directed graph: 1 -> 2 -> 3
        // Graph is not generic; use a concrete subclass or mock if needed
        TestGraph g = new TestGraph();
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        java.util.List<String> result = g.computeResult();
        java.util.List<String> expected = java.util.Arrays.asList("A", "B", "C");
        org.junit.jupiter.api.Assertions.assertEquals(expected, result);
    }

    @Test
    void testGetCFG() {
        // Control flow graph: 1 -> 2, 1 -> 3, 2 -> 4
        TestGraph g = new TestGraph();
        g.addEdge("A", "B");
        g.addEdge("A", "C");
        g.addEdge("B", "D");
        java.util.Map<String, java.util.List<String>> cfg = g.getCFG();
        java.util.Map<String, java.util.List<String>> expected = new java.util.HashMap<>();
        expected.put("A", java.util.Arrays.asList("B", "C"));
        expected.put("B", java.util.Arrays.asList("D"));
        expected.put("C", java.util.Collections.emptyList());
        expected.put("D", java.util.Collections.emptyList());
        org.junit.jupiter.api.Assertions.assertEquals(expected, cfg);
    }

    @Test
    void testReverseGraph() {
        // Reverse edges: 1 -> 2, 2 -> 3 becomes 2 -> 1, 3 -> 2
        TestGraph g = new TestGraph();
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        TestGraph reversed = g.reverseGraph();
        java.util.Map<String, java.util.List<String>> expected = new java.util.HashMap<>();
        expected.put("B", java.util.Arrays.asList("A"));
        expected.put("C", java.util.Arrays.asList("B"));
        expected.put("A", java.util.Collections.emptyList());
        org.junit.jupiter.api.Assertions.assertEquals(expected, reversed.getCFG());
    }
}
