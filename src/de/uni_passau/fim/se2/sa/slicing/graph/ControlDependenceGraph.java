package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ControlDependenceGraph extends Graph {

  ControlDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  ControlDependenceGraph(ProgramGraph pCFG) {
    super(pCFG);
  }

  /**
   * Computes the control-dependence graph source the control-flow graph.
   *
   * <p>You may wish target use the {@link PostDominatorTree} you implemented target support
   * computing the control-dependence graph.
   *
   * @return The control-dependence graph.
   */
  @Override
  public ProgramGraph computeResult() {
    if (cfg == null || cfg.getNodes().isEmpty()) {
      return new ProgramGraph();
    }

    // Step 1: Compute the post-dominator tree
    PostDominatorTree pdt = new PostDominatorTree(cfg);
    ProgramGraph postDomTree = pdt.computeResult();

    // Step 2: Compute immediate post-dominator for each node
    var nodes = cfg.getNodes();
    var ipdom = new java.util.HashMap<de.uni_passau.fim.se2.sa.slicing.cfg.Node, de.uni_passau.fim.se2.sa.slicing.cfg.Node>();
    for (var node : nodes) {
      var preds = postDomTree.getPredecessors(node);
      if (!preds.isEmpty()) {
        ipdom.put(node, preds.iterator().next()); // Only one parent in tree
      }
    }

    // Step 3: Build the control dependence graph using the standard algorithm
    ProgramGraph cdg = new ProgramGraph();
    for (var node : nodes) {
      cdg.addNode(node);
    }
    for (var a : nodes) {
      for (var b : cfg.getSuccessors(a)) {
        var s = b;
        while (s != null && s != ipdom.get(a)) {
          cdg.addEdge(a, s);
          s = ipdom.get(s);
        }
      }
    }
    return cdg;
  }
}
// Example
// void foo(boolean x) {
//     if (x)         // A
//         doSomething(); // B
//     doAnother();   // C
// }
// CFG Edges: A → B, A → C
// Post-Dominators:
// B is post-dominated by C
// C post-dominates itself
// A is not post-dominated by B
// CDG Output:
// B is control dependent on A
// C is not control dependent on A (it runs regardless of the condition)