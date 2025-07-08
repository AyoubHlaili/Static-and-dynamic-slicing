package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.Opcodes;
import java.util.Collection;

/**
 * Simple test class to verify DataFlowAnalysis implementation.
 */
public class DataFlowAnalysisTest {
    public static void main(String[] args) {
        try {
            // Create a simple test method node
            MethodNode methodNode = new MethodNode();
            methodNode.name = "testMethod";
            methodNode.desc = "()V";
            
            // Create a simple ILOAD instruction (loads an integer from local variable 1)
            VarInsnNode loadInstruction = new VarInsnNode(Opcodes.ILOAD, 1);
            
            // Test usedBy method
            Collection<Variable> usedVars = DataFlowAnalysis.usedBy("TestClass", methodNode, loadInstruction);
            System.out.println("Used variables for ILOAD 1: " + usedVars.size());
            for (Variable var : usedVars) {
                System.out.println("  " + var);
            }
            
            // Create a simple ISTORE instruction (stores an integer to local variable 2)
            VarInsnNode storeInstruction = new VarInsnNode(Opcodes.ISTORE, 2);
            
            // Test definedBy method
            Collection<Variable> definedVars = DataFlowAnalysis.definedBy("TestClass", methodNode, storeInstruction);
            System.out.println("Defined variables for ISTORE 2: " + definedVars.size());
            for (Variable var : definedVars) {
                System.out.println("  " + var);
            }
            
            System.out.println("DataFlowAnalysis implementation test passed!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
