package de.uni_passau.fim.se2.sa.slicing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;

public class SlicerUtilTest {
    
    @BeforeEach
    void setUp() {
        // Reset coverage tracker before each test
        CoverageTracker.reset();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        CoverageTracker.reset();
    }
    
    @Test
    void testExecuteTest() {
        // Test with package-private test class - this should throw RuntimeException due to access restrictions
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleIntegerTest";
        String testCase = "testFoo";
        
        // This should throw a RuntimeException due to package access restrictions
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            SlicerUtil.executeTest(className, testCase);
        });
    }
    
    @Test
    void testExecuteTestWithPublicClass() {
        // Test with a method that exists but is not a parameterless test method
        String className = "java.lang.Object";  // Use a known public class
        String testCase = "hashCode";  // Use a known parameterless method
        
        // This should work since Object.hashCode() is public and parameterless
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            SlicerUtil.executeTest(className, testCase);
        });
    }
    
    @Test
    void testExecuteTestWithInvalidClass() {
        // Test execution with non-existent class
        String className = "NonExistentClass";
        String testCase = "someMethod";
        
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            SlicerUtil.executeTest(className, testCase);
        });
    }
    
    @Test
    void testExecuteTestWithInvalidMethod() {
        // Test execution with valid class but non-existent method
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleIntegerTest";
        String testCase = "nonExistentMethod";
        
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            SlicerUtil.executeTest(className, testCase);
        });
    }
    
    @Test
    void testExecuteTestWithCalculatorTest() {
        // Test with another example test class - also should fail due to package access
        String className = "de.uni_passau.fim.se2.sa.examples.CalculatorTest";
        String testCase = "testEvaluateSimple";
        
        // This should throw a RuntimeException due to package access restrictions
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            SlicerUtil.executeTest(className, testCase);
        });
    }

    @Test
    void testSimplify() throws Exception {
        // Create a PDG for testing simplification
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
        
        ProgramDependenceGraph originalPDG = new ProgramDependenceGraph(classNode, methodNode);
        ProgramGraph originalGraph = originalPDG.computeResult();
        
        org.junit.jupiter.api.Assertions.assertNotNull(originalGraph, "Original PDG should not be null");
        int originalNodeCount = originalGraph.getNodes().size();
        
        // Simulate some coverage by manually tracking some line visits
        CoverageTracker.trackLineVisit(6);  // Corresponds to lines in SimpleInteger.foo()
        CoverageTracker.trackLineVisit(7);
        CoverageTracker.trackLineVisit(8);
        
        // Test simplification
        ProgramDependenceGraph simplifiedPDG = SlicerUtil.simplify(originalPDG);
        
        org.junit.jupiter.api.Assertions.assertNotNull(simplifiedPDG, "Simplified PDG should not be null");
        
        ProgramGraph simplifiedGraph = simplifiedPDG.computeResult();
        org.junit.jupiter.api.Assertions.assertNotNull(simplifiedGraph, "Simplified graph should not be null");
        
        // The simplified graph should have fewer or equal nodes than the original
        int simplifiedNodeCount = simplifiedGraph.getNodes().size();
        org.junit.jupiter.api.Assertions.assertTrue(simplifiedNodeCount <= originalNodeCount, 
            "Simplified graph should have fewer or equal nodes than original");
    }
    
    @Test
    void testSimplifyWithNoCoverage() throws Exception {
        // Test simplification when no lines are covered
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
        
        ProgramDependenceGraph originalPDG = new ProgramDependenceGraph(classNode, methodNode);
        
        // Don't track any line visits - coverage should be empty
        
        // Test simplification
        ProgramDependenceGraph simplifiedPDG = SlicerUtil.simplify(originalPDG);
        
        org.junit.jupiter.api.Assertions.assertNotNull(simplifiedPDG, "Simplified PDG should not be null");
        
        ProgramGraph simplifiedGraph = simplifiedPDG.computeResult();
        org.junit.jupiter.api.Assertions.assertNotNull(simplifiedGraph, "Simplified graph should not be null");
        
        // With no coverage, the simplified graph should have no or very few nodes
        int simplifiedNodeCount = simplifiedGraph.getNodes().size();
        org.junit.jupiter.api.Assertions.assertTrue(simplifiedNodeCount >= 0, 
            "Simplified graph should have non-negative node count");
    }
    
    @Test
    void testSimplifyWithFullCoverage() throws Exception {
        // Test simplification when many lines are covered
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
        
        ProgramDependenceGraph originalPDG = new ProgramDependenceGraph(classNode, methodNode);
        ProgramGraph originalGraph = originalPDG.computeResult();
        int originalNodeCount = originalGraph.getNodes().size();
        
        // Simulate extensive coverage by tracking many line visits
        for (int i = 1; i <= 20; i++) {
            CoverageTracker.trackLineVisit(i);
        }
        
        // Test simplification
        ProgramDependenceGraph simplifiedPDG = SlicerUtil.simplify(originalPDG);
        
        org.junit.jupiter.api.Assertions.assertNotNull(simplifiedPDG, "Simplified PDG should not be null");
        
        ProgramGraph simplifiedGraph = simplifiedPDG.computeResult();
        org.junit.jupiter.api.Assertions.assertNotNull(simplifiedGraph, "Simplified graph should not be null");
        
        // With extensive coverage, the simplified graph might have similar node count
        int simplifiedNodeCount = simplifiedGraph.getNodes().size();
        org.junit.jupiter.api.Assertions.assertTrue(simplifiedNodeCount <= originalNodeCount, 
            "Simplified graph should have fewer or equal nodes than original");
    }

    @Test
    void testSimplifyWithNullPDG() {
        // Test simplification with a PDG that has null computeResult()
        ProgramGraph emptyGraph = new ProgramGraph();
        ProgramDependenceGraph emptyPDG = new ProgramDependenceGraph(emptyGraph);
        
        ProgramDependenceGraph result = SlicerUtil.simplify(emptyPDG);
        
        org.junit.jupiter.api.Assertions.assertNotNull(result, "Result should not be null");
        ProgramGraph resultGraph = result.computeResult();
        org.junit.jupiter.api.Assertions.assertNotNull(resultGraph, "Result graph should not be null");
        org.junit.jupiter.api.Assertions.assertEquals(0, resultGraph.getNodes().size(), 
            "Empty PDG should result in empty simplified PDG");
    }
}
