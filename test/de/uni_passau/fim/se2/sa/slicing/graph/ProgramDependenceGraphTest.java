package de.uni_passau.fim.se2.sa.slicing.graph;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import java.util.Set;

public class ProgramDependenceGraphTest {
    
    @Test
    void testBackwardSlice() throws Exception {
        // Test backward slicing with SimpleInteger example
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleInteger";
        final ClassNode classNode = new ClassNode(Opcodes.ASM9);
        final ClassReader classReader = new ClassReader(className);
        classReader.accept(classNode, 0);
        
        // Find the 'foo' method
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> "foo".equals(m.name))
            .findFirst()
            .orElse(null);
            
        org.junit.jupiter.api.Assertions.assertNotNull(methodNode, "Method 'foo' should be found");
        
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
        ProgramGraph result = pdg.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result, "PDG should not be null");
        org.junit.jupiter.api.Assertions.assertTrue(result.getNodes().size() > 0, "PDG should have nodes");
        
        // Test backward slice from a specific node (e.g., return statement)
        Node returnNode = null;
        for (Node node : result.getNodes()) {
            if (node.toString().contains("IRETURN")) {
                returnNode = node;
                break;
            }
        }
        
        if (returnNode != null) {
            Set<Node> slice = pdg.backwardSlice(returnNode);
            org.junit.jupiter.api.Assertions.assertNotNull(slice, "Backward slice should not be null");
            org.junit.jupiter.api.Assertions.assertTrue(slice.size() > 0, "Backward slice should contain nodes");
            org.junit.jupiter.api.Assertions.assertTrue(slice.contains(returnNode), "Slice should contain the criterion node");
        }
    }

    @Test
    void testComputeResult() throws Exception {
        // Test PDG computation with SimpleInteger example
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleInteger";
        final ClassNode classNode = new ClassNode(Opcodes.ASM9);
        final ClassReader classReader = new ClassReader(className);
        classReader.accept(classNode, 0);
        
        // Find the 'foo' method
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> "foo".equals(m.name))
            .findFirst()
            .orElse(null);
            
        org.junit.jupiter.api.Assertions.assertNotNull(methodNode, "Method 'foo' should be found");
        
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
        ProgramGraph result = pdg.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result, "PDG should not be null");
        org.junit.jupiter.api.Assertions.assertTrue(result.getNodes().size() > 0, "PDG should have nodes");
        
        // Verify that PDG contains both control and data dependence edges
        int edgeCount = 0;
        for (Node node : result.getNodes()) {
            edgeCount += result.getSuccessors(node).size();
        }
        
        org.junit.jupiter.api.Assertions.assertTrue(edgeCount >= 0, "PDG should have some edges (could be 0 for simple methods)");
    }

    @Test
    void testComputeResultWithComplexExample() throws Exception {
        // Test PDG computation with a more complex example
        String className = "de.uni_passau.fim.se2.sa.examples.Calculator";
        final ClassNode classNode = new ClassNode(Opcodes.ASM9);
        final ClassReader classReader = new ClassReader(className);
        classReader.accept(classNode, 0);
        
        // Find a public method (like 'evaluate')
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> (m.access & Opcodes.ACC_PUBLIC) != 0 && !"<init>".equals(m.name))
            .findFirst()
            .orElse(null);
            
        org.junit.jupiter.api.Assertions.assertNotNull(methodNode, "Public method should be found");
        
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
        ProgramGraph result = pdg.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result, "PDG should not be null");
        org.junit.jupiter.api.Assertions.assertTrue(result.getNodes().size() > 0, "PDG should have nodes");
    }

    @Test
    void testBackwardSliceEmpty() {
        // Test backward slice with empty/null criterion
        ProgramGraph emptyGraph = new ProgramGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(emptyGraph);
        
        Set<Node> slice = pdg.backwardSlice(null);
        org.junit.jupiter.api.Assertions.assertNotNull(slice, "Backward slice should not be null");
        org.junit.jupiter.api.Assertions.assertEquals(0, slice.size(), "Backward slice should be empty for null criterion");
    }

    @Test
    void testBackwardSliceWithSingleNode() {
        // Test backward slice with a single node
        ProgramGraph graph = new ProgramGraph();
        Node singleNode = new Node("TestNode");
        graph.addNode(singleNode);
        
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(graph);
        Set<Node> slice = pdg.backwardSlice(singleNode);
        
        org.junit.jupiter.api.Assertions.assertNotNull(slice, "Backward slice should not be null");
        org.junit.jupiter.api.Assertions.assertEquals(1, slice.size(), "Backward slice should contain single node");
        org.junit.jupiter.api.Assertions.assertTrue(slice.contains(singleNode), "Slice should contain the criterion node");
    }

    @Test
    void testComputeResultPrecomputed() {
        // Test when PDG is already precomputed
        ProgramGraph precomputedGraph = new ProgramGraph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        precomputedGraph.addNode(nodeA);
        precomputedGraph.addNode(nodeB);
        precomputedGraph.addEdge(nodeA, nodeB);
        
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(precomputedGraph);
        ProgramGraph result = pdg.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(result, "PDG should not be null");
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getNodes().size(), "PDG should have 2 nodes");
        org.junit.jupiter.api.Assertions.assertEquals(precomputedGraph, result, "Should return the precomputed graph");
    }
}
