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

    // Step 2: Compute post-dominator sets for each node
    // use a map: node -> set of post-dominators
    var postDominators = new java.util.HashMap<de.uni_passau.fim.se2.sa.slicing.cfg.Node, java.util.Set<de.uni_passau.fim.se2.sa.slicing.cfg.Node>>();
    var nodes = cfg.getNodes();
    for (var node : nodes) {
      postDominators.put(node, new java.util.HashSet<>());
    }
    // For each node, traverse up the post-dominator tree to collect all post-dominators
    for (var node : nodes) {
      var current = node;
      while (true) {
        postDominators.get(node).add(current);
        var parents = postDomTree.getPredecessors(current);
        if (parents.isEmpty()) break;
        // In a tree, there should be only one parent
        current = parents.iterator().next();
      }
    }

    // Step 3: Build the control dependence graph
    ProgramGraph cdg = new ProgramGraph();
    for (var node : nodes) {
      cdg.addNode(node);
    }
    for (var a : nodes) {
      for (var b : cfg.getSuccessors(a)) {
        // For each node x in the CFG, for each successor y,
        // if x does not post-dominate y, then y is control dependent on x
        if (!postDominators.get(b).contains(a)) {
          cdg.addEdge(a, b);
        }
      }
    }
    return cdg;
  }
}
