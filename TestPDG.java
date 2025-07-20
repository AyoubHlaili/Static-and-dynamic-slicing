import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class TestPDG {
    public static void main(String[] args) throws Exception {
        // Test with Calculator class
        String className = "de.uni_passau.fim.se2.sa.examples.Calculator";
        final ClassNode classNode = new ClassNode(Opcodes.ASM9);
        final ClassReader classReader = new ClassReader(className);
        classReader.accept(classNode, 0);
        
        // Find the add method
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> "add".equals(m.name))
            .findFirst()
            .orElse(null);
            
        if (methodNode != null) {
            ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
            ProgramGraph result = pdg.computeResult();
            
            System.out.println("ProgramDependenceGraph computed successfully!");
            System.out.println("Number of nodes: " + result.getNodes().size());
            
            // Test backward slice
            if (!result.getNodes().isEmpty()) {
                Node firstNode = result.getNodes().iterator().next();
                java.util.Set<Node> slice = pdg.backwardSlice(firstNode);
                System.out.println("Backward slice from first node contains " + slice.size() + " nodes");
            }
        } else {
            System.out.println("Method 'add' not found!");
        }
    }
}
