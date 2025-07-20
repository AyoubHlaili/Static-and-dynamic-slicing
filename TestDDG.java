import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.graph.DataDependenceGraph;
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
        
        // Find the add method
        MethodNode methodNode = classNode.methods.stream()
            .filter(m -> "add".equals(m.name))
            .findFirst()
            .orElse(null);
            
        if (methodNode != null) {
            DataDependenceGraph ddg = new DataDependenceGraph(classNode, methodNode);
            ProgramGraph result = ddg.computeResult();
            
            if (result != null) {
                System.out.println("DataDependenceGraph computed successfully!");
                System.out.println("Number of nodes: " + result.getNodes().size());
                System.out.println("CFG has " + ddg.getCFG().getNodes().size() + " nodes");
                
                // Check if there are any data dependencies
                int edgeCount = 0;
                for (Node node : result.getNodes()) {
                    edgeCount += result.getSuccessors(node).size();
                }
                System.out.println("Number of data dependence edges: " + edgeCount);
            } else {
                System.out.println("DataDependenceGraph result is null");
            }
        } else {
            System.out.println("Method 'add' not found!");
        }
    }
}
