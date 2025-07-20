package de.uni_passau.fim.se2.sa.slicing;

import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;

public class SlicerUtil {

    /**
     * Executes the provided test case on the given class via the JUnit test framework.
     *
     * @param className The name of the class to be tested.
     * @param testCase  The name of the test case to be executed.
     */
    public static void executeTest(String className, String testCase) {
        try {
            Class<?> clazz = Class.forName(className);
            Object testInstance = clazz.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method method = clazz.getMethod(testCase);
            method.invoke(testInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute test: " + className + "#" + testCase, e);
        }
    }

    /**
     * Simplifies the given program dependence graph by removing all nodes and corresponding edges
     * that were not covered by the executed test.
     *
     * @param pPDG The program dependence graph to simplify.
     * @return The simplified program dependence graph.
     */
    public static ProgramDependenceGraph simplify(final ProgramDependenceGraph pPDG) {
        // Get the original PDG
        de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph originalGraph = pPDG.computeResult();
        
        // Get the visited lines from the coverage tracker
        java.util.Set<Integer> visitedLines = de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker.getVisitedLines();
        
        // Filter nodes based on coverage: only keep nodes whose line numbers were visited
        java.util.Set<de.uni_passau.fim.se2.sa.slicing.cfg.Node> coveredNodes = new java.util.HashSet<>();
        if (originalGraph != null) {
            for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : originalGraph.getNodes()) {
                // Check if this node's line number was covered during execution
                int lineNumber = node.getLineNumber();
                if (lineNumber > 0 && visitedLines.contains(lineNumber)) {
                    coveredNodes.add(node);
                }
            }
        }
        
        // Create a new reduced PDG containing only covered nodes and their edges
        de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph reducedGraph = 
            new de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph();
        
        // Add only covered nodes
        for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : coveredNodes) {
            reducedGraph.addNode(node);
        }
        
        // Add only edges between covered nodes
        for (de.uni_passau.fim.se2.sa.slicing.cfg.Node src : coveredNodes) {
            for (de.uni_passau.fim.se2.sa.slicing.cfg.Node tgt : originalGraph.getSuccessors(src)) {
                if (coveredNodes.contains(tgt)) {
                    reducedGraph.addEdge(src, tgt);
                }
            }
        }
        
        return new ProgramDependenceGraph(reducedGraph);
    }
}
