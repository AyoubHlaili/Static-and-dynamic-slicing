import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class TestDDG {
    public static void main(String[] args) throws Exception {
        // Test with Calculator class
        String className = "de.uni_passau.fim.se2.sa.examples.Calculator";
        final ClassNode classNode = new ClassNode(Opcodes.ASM9);
        final ClassReader classReader = new ClassReader(className);
        classReader.accept(classNode, 0);
        
        // Find the evaluate method
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> "evaluate".equals(m.name))
            .findFirst()
            .orElse(null);
            
        if (methodNode != null) {
            // Test through ProgramDependenceGraph which includes DataDependenceGraph
            ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
            ProgramGraph result = pdg.computeResult();
            
            if (result != null) {
                System.out.println("ProgramDependenceGraph (including DataDependenceGraph) computed successfully!");
                System.out.println("Number of nodes: " + result.getNodes().size());
                System.out.println("CFG has " + pdg.getCFG().getNodes().size() + " nodes");
                
                // Check if there are any dependencies
                int edgeCount = 0;
                for (Node node : result.getNodes()) {
                    edgeCount += result.getSuccessors(node).size();
                }
                System.out.println("Number of total dependence edges: " + edgeCount);
                
                // Test backward slice
                if (!result.getNodes().isEmpty()) {
                    Node firstNode = result.getNodes().iterator().next();
                    java.util.Set<Node> slice = pdg.backwardSlice(firstNode);
                    System.out.println("Backward slice from first node contains " + slice.size() + " nodes");
                }
            } else {
                System.out.println("ProgramDependenceGraph result is null");
            }
        } else {
            System.out.println("Method 'evaluate' not found!");
        }
    }
}
