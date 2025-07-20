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
        // TODO Implement execution of test method here
        throw new UnsupportedOperationException("Execution of test method missing");
    }

    /**
     * Simplifies the given program dependence graph by removing all nodes and corresponding edges
     * that were not covered by the executed test.
     *
     * @param pPDG The program dependence graph to simplify.
     * @return The simplified program dependence graph.
     */
    public static ProgramDependenceGraph simplify(final ProgramDependenceGraph pPDG, java.util.Set<de.uni_passau.fim.se2.sa.slicing.cfg.Node> coveredNodes) {
        // Create a new ProgramDependenceGraph containing only the covered nodes and edges between them
        de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph originalGraph = pPDG.computeResult();
        de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph reducedGraph = new de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph();
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
