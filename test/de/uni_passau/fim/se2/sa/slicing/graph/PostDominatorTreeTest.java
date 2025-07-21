package de.uni_passau.fim.se2.sa.slicing.graph;

import org.junit.jupiter.api.Test;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;

public class PostDominatorTreeTest {
    @Test
    void testComputeResult() {
        // Create a simple CFG: A -> B -> C (linear)
        ProgramGraph cfg = createLinearCFG();
        PostDominatorTree pdt = new PostDominatorTree(cfg);
        
        ProgramGraph result = pdt.computeResult();
        
        // In a linear graph A->B->C, the post-dominator tree should have:
        // C post-dominates B and A, B post-dominates A
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(3, result.getNodes().size());
    }

    @Test
    void testComputeResultWithBranching() {
        // Create a CFG with branching: A -> B, A -> C, B -> D, C -> D
        ProgramGraph cfg = createBranchingCFG();
        PostDominatorTree pdt = new PostDominatorTree(cfg);
        
        ProgramGraph result = pdt.computeResult();
        
        // D should post-dominate all nodes since all paths must go through D
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(4, result.getNodes().size());
    }

    @Test
    void testComputeResultEmptyCFG() {
        // Test with empty CFG
        ProgramGraph emptyCfg = new ProgramGraph();
        PostDominatorTree pdt = new PostDominatorTree(emptyCfg);
        
        ProgramGraph result = pdt.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(0, result.getNodes().size());
    }

    @Test
    void testComputeResultSingleNode() {
        // Test with single node CFG
        ProgramGraph singleNodeCfg = createSingleNodeCFG();
        PostDominatorTree pdt = new PostDominatorTree(singleNodeCfg);
        
        ProgramGraph result = pdt.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(1, result.getNodes().size());
    }

    @Test
    void testComputeResultWithLoop() {
        // Test with a simple loop: A -> B -> C, C -> B, C -> D
        ProgramGraph loopCfg = createLoopCFG();
        PostDominatorTree pdt = new PostDominatorTree(loopCfg);
        
        ProgramGraph result = pdt.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(4, result.getNodes().size());
    }

    // Helper methods to create test CFGs
    private ProgramGraph createLinearCFG() {
        ProgramGraph cfg = new ProgramGraph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        
        cfg.addNode(nodeA);
        cfg.addNode(nodeB);
        cfg.addNode(nodeC);
        cfg.addEdge(nodeA, nodeB);
        cfg.addEdge(nodeB, nodeC);
        
        return cfg;
    }

    private ProgramGraph createBranchingCFG() {
        ProgramGraph cfg = new ProgramGraph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        
        cfg.addNode(nodeA);
        cfg.addNode(nodeB);
        cfg.addNode(nodeC);
        cfg.addNode(nodeD);
        cfg.addEdge(nodeA, nodeB);
        cfg.addEdge(nodeA, nodeC);
        cfg.addEdge(nodeB, nodeD);
        cfg.addEdge(nodeC, nodeD);
        
        return cfg;
    }

    private ProgramGraph createSingleNodeCFG() {
        ProgramGraph cfg = new ProgramGraph();
        Node nodeA = new Node("A");
        cfg.addNode(nodeA);
        return cfg;
    }

    private ProgramGraph createLoopCFG() {
        ProgramGraph cfg = new ProgramGraph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        
        cfg.addNode(nodeA);
        cfg.addNode(nodeB);
        cfg.addNode(nodeC);
        cfg.addNode(nodeD);
        cfg.addEdge(nodeA, nodeB);
        cfg.addEdge(nodeB, nodeC);
        cfg.addEdge(nodeC, nodeB); // Loop back
        cfg.addEdge(nodeC, nodeD); // Exit from loop
        
        return cfg;
    }
}
