package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** Provides an analysis that calculates the program-dependence graph. */
public class ProgramDependenceGraph extends Graph implements Sliceable<Node> {

  private ProgramGraph pdg;
  private final ProgramGraph cdg;
  private final ProgramGraph ddg;

  public ProgramDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
    pdg = null;

    if (cfg != null) {
      cdg = new ControlDependenceGraph(pClassNode, pMethodNode).computeResult();
      ddg = new DataDependenceGraph(pClassNode, pMethodNode).computeResult();
    } else {
      cdg = null;
      ddg = null;
    }
  }

  public ProgramDependenceGraph(ProgramGraph pProgramGraph) {
    super(null);
    pdg = pProgramGraph;
    cdg = null;
    ddg = null;
  }

  /**
   * Computes the program-dependence graph from a control-flow graph.
   *
   * <p>You may wish to use the {@link ControlDependenceGraph} and {@link DataDependenceGraph} you
   * have already implemented to support computing the program-dependence graph.
   *
   * @return A program-dependence graph.
   */
  @Override
  public ProgramGraph computeResult() {
    if (pdg != null) {
      return pdg;
    }
    if (cdg == null && ddg == null) {
      return null;
    }
    // Create a new ProgramGraph for the PDG
    pdg = new ProgramGraph();
    // Add all nodes from both CDG and DDG (union)
    java.util.Set<Node> allNodes = new java.util.HashSet<>();
    allNodes.addAll(cdg.getNodes());
    allNodes.addAll(ddg.getNodes());
    for (Node node : allNodes) {
      pdg.addNode(node);
    }
    // Add all control dependence edges
    for (Node src : cdg.getNodes()) {
      for (Node tgt : cdg.getSuccessors(src)) {
        pdg.addEdge(src, tgt);
      }
    }
    // Add all data dependence edges
    for (Node src : ddg.getNodes()) {
      for (Node tgt : ddg.getSuccessors(src)) {
        // Avoid duplicate edges (if already present from CDG)
        if (!pdg.getSuccessors(src).contains(tgt)) {
          pdg.addEdge(src, tgt);
        }
      }
    }
    return pdg;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Node> backwardSlice(Node pCriterion) {
    ProgramGraph pdgGraph = computeResult();
    Set<Node> slice = new java.util.HashSet<>();
    if (pdgGraph == null || pCriterion == null) {
      return slice;
    }
    // Worklist algorithm: traverse PDG backward from pCriterion
    java.util.Deque<Node> worklist = new java.util.ArrayDeque<>();
    worklist.add(pCriterion);
    slice.add(pCriterion);
    while (!worklist.isEmpty()) {
      Node current = worklist.remove();
      for (Node pred : pdgGraph.getPredecessors(current)) {
        if (slice.add(pred)) {
          worklist.add(pred);
        }
      }
    }
    return slice;
  }
}
