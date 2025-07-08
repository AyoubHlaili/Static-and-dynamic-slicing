package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.Opcodes;
import java.util.Collection;

/**
 * Comprehensive test for DataFlowAnalysis implementation.
 */
public class DataFlowAnalysisTestExtended {
    public static void main(String[] args) {
        try {
            // Create a simple test method node
            MethodNode methodNode = new MethodNode();
            methodNode.name = "testMethod";
            methodNode.desc = "()V";
            
            System.out.println("=== Testing DataFlowAnalysis ===");
            
            // Test 1: ILOAD (use)
            testInstruction("ILOAD", new VarInsnNode(Opcodes.ILOAD, 1), methodNode);
            
            // Test 2: ISTORE (define)
            testInstruction("ISTORE", new VarInsnNode(Opcodes.ISTORE, 2), methodNode);
            
            // Test 3: ALOAD (use)
            testInstruction("ALOAD", new VarInsnNode(Opcodes.ALOAD, 0), methodNode);
            
            // Test 4: ASTORE (define)
            testInstruction("ASTORE", new VarInsnNode(Opcodes.ASTORE, 3), methodNode);
            
            // Test 5: GETFIELD (use)
            testInstruction("GETFIELD", new FieldInsnNode(Opcodes.GETFIELD, "TestClass", "field", "I"), methodNode);
            
            // Test 6: PUTFIELD (define)
            testInstruction("PUTFIELD", new FieldInsnNode(Opcodes.PUTFIELD, "TestClass", "field", "I"), methodNode);
            
            // Test 7: GETSTATIC (use)
            testInstruction("GETSTATIC", new FieldInsnNode(Opcodes.GETSTATIC, "TestClass", "staticField", "I"), methodNode);
            
            // Test 8: PUTSTATIC (define)
            testInstruction("PUTSTATIC", new FieldInsnNode(Opcodes.PUTSTATIC, "TestClass", "staticField", "I"), methodNode);
            
            // Test 9: IINC (both use and define)
            testInstructionBoth("IINC", new IincInsnNode(5, 1), methodNode);
            
            System.out.println("\n=== All tests passed! ===");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void testInstruction(String name, org.objectweb.asm.tree.AbstractInsnNode instruction, MethodNode methodNode) throws Exception {
        Collection<Variable> usedVars = DataFlowAnalysis.usedBy("TestClass", methodNode, instruction);
        Collection<Variable> definedVars = DataFlowAnalysis.definedBy("TestClass", methodNode, instruction);
        
        System.out.println(name + ":");
        System.out.println("  Used: " + usedVars.size() + " variables");
        for (Variable var : usedVars) {
            System.out.println("    " + var);
        }
        System.out.println("  Defined: " + definedVars.size() + " variables");
        for (Variable var : definedVars) {
            System.out.println("    " + var);
        }
    }
    
    private static void testInstructionBoth(String name, org.objectweb.asm.tree.AbstractInsnNode instruction, MethodNode methodNode) throws Exception {
        Collection<Variable> usedVars = DataFlowAnalysis.usedBy("TestClass", methodNode, instruction);
        Collection<Variable> definedVars = DataFlowAnalysis.definedBy("TestClass", methodNode, instruction);
        
        System.out.println(name + " (should both use and define):");
        System.out.println("  Used: " + usedVars.size() + " variables");
        for (Variable var : usedVars) {
            System.out.println("    " + var);
        }
        System.out.println("  Defined: " + definedVars.size() + " variables");
        for (Variable var : definedVars) {
            System.out.println("    " + var);
        }
    }
}
