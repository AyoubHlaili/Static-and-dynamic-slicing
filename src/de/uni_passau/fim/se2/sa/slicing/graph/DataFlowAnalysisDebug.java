package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import java.util.Collection;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class DataFlowAnalysisDebug {
    
    public static void debugInstruction(String className, MethodNode method, AbstractInsnNode instruction, int index) {
        System.out.println("=== Instruction " + index + " ===");
        System.out.println("Class: " + instruction.getClass().getSimpleName());
        System.out.println("Opcode: " + instruction.getOpcode());
        System.out.println("Type: " + instruction.getType());
        
        try {
            Collection<Variable> defined = DataFlowAnalysis.definedBy(className, method, instruction);
            Collection<Variable> used = DataFlowAnalysis.usedBy(className, method, instruction);
            
            System.out.println("Defined variables (" + defined.size() + "):");
            for (Variable v : defined) {
                System.out.println("  " + v);
            }
            
            System.out.println("Used variables (" + used.size() + "):");
            for (Variable v : used) {
                System.out.println("  " + v);
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();
    }
}
