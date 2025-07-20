package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

class DataDependenceGraphDebugTest {

    @Test
    void debugSimpleIntegerDependencies() throws Exception {
        // Test with SimpleInteger class to debug dependencies
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleInteger";
        final ClassNode classNode = new ClassNode(Opcodes.ASM9);
        final ClassReader classReader = new ClassReader(className);
        classReader.accept(classNode, 0);
        
        // Find the foo method
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> "foo".equals(m.name))
            .findFirst()
            .orElse(null);
            
        assertNotNull(methodNode, "Method 'foo' should be found");
        
        // Create DDG directly to inspect its behavior
        DataDependenceGraph ddgAnalysis = new DataDependenceGraph(classNode, methodNode);
        ProgramGraph ddg = ddgAnalysis.computeResult();
        
        assertNotNull(ddg, "DataDependenceGraph should not be null");
        
        // Debug: Print detailed information about definitions and uses
        System.out.println("=== DDG Analysis Debug ===");
        System.out.println("Total nodes: " + ddg.getNodes().size());
        
        // Print all nodes with their line numbers and instructions
        System.out.println("\n=== All Nodes ===");
        for (Node node : ddg.getNodes()) {
            System.out.println("Node " + node.getID() + " (line " + node.getLineNumber() + "): " + 
                             node.toString().replaceAll("\\s+", " "));
            
            // Check what variables are defined and used by this node
            var definedVars = DataFlowAnalysis.definedBy(classNode.name, methodNode, node.getInstruction());
            var usedVars = DataFlowAnalysis.usedBy(classNode.name, methodNode, node.getInstruction());
            
            if (!definedVars.isEmpty()) {
                System.out.println("  Defines: ");
                for (var v : definedVars) {
                    System.out.println("    " + v.toString() + " [hash=" + v.hashCode() + ", identity=" + System.identityHashCode(v) + "]");
                }
            }
            if (!usedVars.isEmpty()) {
                System.out.println("  Uses: ");
                for (var v : usedVars) {
                    System.out.println("    " + v.toString() + " [hash=" + v.hashCode() + ", identity=" + System.identityHashCode(v) + "]");
                }
            }
        }
        
        // Print dependencies
        System.out.println("\n=== Dependencies ===");
        int totalEdges = 0;
        for (Node node : ddg.getNodes()) {
            var successors = ddg.getSuccessors(node);
            if (!successors.isEmpty()) {
                totalEdges += successors.size();
                System.out.println("Node " + node.getID() + " (line " + node.getLineNumber() + ") -> " + 
                                 successors.size() + " dependencies");
                for (Node successor : successors) {
                    System.out.println("  -> Node " + successor.getID() + " (line " + successor.getLineNumber() + ")");
                }
            }
        }
        
        System.out.println("Total data dependence edges: " + totalEdges);
        
        // We expect at least some dependencies for this method
        // Line 8 (c = a + b) should depend on lines 6 (a) and 7 (b)
        // Line 10 (d = b - c) should depend on line 9 (b) and line 8 (c)
        // Line 11 (return) should depend on lines 8, 9, and 10
        
        assertTrue(totalEdges > 0, "Should have data dependencies in SimpleInteger.foo()");
    }
}
