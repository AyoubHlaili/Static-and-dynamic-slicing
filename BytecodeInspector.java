import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import de.uni_passau.fim.se2.sa.slicing.graph.DataFlowAnalysis;
import br.usp.each.saeg.asm.defuse.Variable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class BytecodeInspector {
    public static void main(String[] args) {
        try {
            // Load the SimpleInteger class
            InputStream is = BytecodeInspector.class.getResourceAsStream("/de/uni_passau/fim/se2/sa/examples/SimpleInteger.class");
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
                    System.out.println("Instructions:");
                    
                    int index = 0;
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn != null) {
                        System.out.println("Index " + index + ": " + insn.getClass().getSimpleName() + 
                                         " (opcode: " + insn.getOpcode() + ")");
                        
                        // Test our DataFlowAnalysis methods
                        if (index == 0 || index == 10) {
                            try {
                                Collection<Variable> defined = DataFlowAnalysis.definedBy(
                                    classNode.name, method, insn);
                                Collection<Variable> used = DataFlowAnalysis.usedBy(
                                    classNode.name, method, insn);
                                
                                System.out.println("  - Defined variables: " + defined.size());
                                System.out.println("  - Used variables: " + used.size());
                                
                                for (Variable v : defined) {
                                    System.out.println("    Defined: " + v);
                                }
                                for (Variable v : used) {
                                    System.out.println("    Used: " + v);
                                }
                            } catch (Exception e) {
                                System.out.println("  Error: " + e.getMessage());
                            }
                        }
                        
                        insn = insn.getNext();
                        index++;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
