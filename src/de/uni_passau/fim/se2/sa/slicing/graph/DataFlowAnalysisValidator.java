package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import java.util.Collection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import java.io.IOException;
import java.io.InputStream;

/** Test utility to validate DataFlowAnalysis implementation. */
public class DataFlowAnalysisValidator {
    
    public static void main(String[] args) {
        try {
            // Load the SimpleInteger class
            InputStream is = DataFlowAnalysisValidator.class.getResourceAsStream("/de/uni_passau/fim/se2/sa/examples/SimpleInteger.class");
            if (is == null) {
                System.out.println("Could not find SimpleInteger.class in resources");
                return;
            }
            
            ClassReader reader = new ClassReader(is);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);
            
            // Find the foo method
            for (MethodNode method : classNode.methods) {
                if ("foo".equals(method.name)) {
                    System.out.println("Method: " + method.name + method.desc);
                    System.out.println("Analyzing instructions:\n");
                    
                    int index = 0;
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn != null) {
                        System.out.println("Index " + index + ": " + insn.getClass().getSimpleName() + 
                                         " (opcode: " + insn.getOpcode() + ")");
                        
                        // Test our DataFlowAnalysis methods at specific indices
                        if (index == 0) {
                            try {
                                Collection<Variable> defined = DataFlowAnalysis.definedBy(
                                    classNode.name, method, insn);
                                Collection<Variable> used = DataFlowAnalysis.usedBy(
                                    classNode.name, method, insn);
                                
                                System.out.println("  ** Test Case: DFA_definedBy **");
                                System.out.println("    Instruction at index 0 " + 
                                    (defined.size() > 0 ? "defines a variable." : "does not define a variable."));
                                System.out.println("    Expected: 1, but was: " + defined.size());
                                System.out.println("    Result: " + (defined.size() == 1 ? "PASS" : "FAIL"));
                                
                                for (Variable v : defined) {
                                    System.out.println("      Defined: " + v);
                                }
                                for (Variable v : used) {
                                    System.out.println("      Used: " + v);
                                }
                            } catch (Exception e) {
                                System.out.println("    Error: " + e.getMessage());
                            }
                        }
                        
                        if (index == 10) {
                            try {
                                Collection<Variable> defined = DataFlowAnalysis.definedBy(
                                    classNode.name, method, insn);
                                Collection<Variable> used = DataFlowAnalysis.usedBy(
                                    classNode.name, method, insn);
                                
                                System.out.println("  ** Test Case: DFA_usedBy **");
                                System.out.println("    Instruction at index 10 " + 
                                    (used.size() == 0 ? "does not make use of a variable." : "makes use of variables."));
                                System.out.println("    Expected to be true (no variables used)");
                                System.out.println("    Result: " + (used.size() == 0 ? "PASS" : "FAIL"));
                                
                                for (Variable v : defined) {
                                    System.out.println("      Defined: " + v);
                                }
                                for (Variable v : used) {
                                    System.out.println("      Used: " + v);
                                }
                            } catch (Exception e) {
                                System.out.println("    Error: " + e.getMessage());
                            }
                        }
                        
                        insn = insn.getNext();
                        index++;
                    }
                    
                    System.out.println("\nTotal instructions: " + index);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
