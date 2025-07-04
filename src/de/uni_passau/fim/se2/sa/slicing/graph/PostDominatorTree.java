package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** Provides an analysis computing a post-dominator tree for a CFG. */
public class PostDominatorTree extends Graph {

  PostDominatorTree(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  PostDominatorTree(ProgramGraph pCFG) {
    super(pCFG);
  }

  /**
   * Computes the post-dominator tree of the method.
   *
   * <p>The implementation uses the {@link #cfg} graph as the starting point.
   *
   * @return The post-dominator tree of the control-flow graph
   */
  @Override
  public ProgramGraph computeResult() {
    if (cfg == null || cfg.getNodes().isEmpty()) {
      return new ProgramGraph();
    }

    // Step 1: Reverse the CFG to convert post-dominance to dominance
    ProgramGraph reversedCFG = reverseGraph(cfg);
    
    // Step 2: Compute the dominator tree of the reversed graph
    return computeDominatorTree(reversedCFG);
  }
  
  /**
   * Computes the dominator tree using the Lengauer-Tarjan algorithm (simplified version).
   * 
   * @param graph The graph to compute dominators for
   * @return The dominator tree
   */
  private ProgramGraph computeDominatorTree(ProgramGraph graph) {
    if (graph.getNodes().isEmpty()) {
      return new ProgramGraph();
    }
    
    // Find the entry node (should be the exit node of the original CFG after reversal)
    var entryOpt = graph.getEntry();
    if (entryOpt.isEmpty()) {
      return new ProgramGraph();
    }
    
    var entry = entryOpt.get();
    var nodes = graph.getNodes();
    
    // Initialize dominator sets - each node is initially dominated by all nodes
    var dominators = new java.util.HashMap<Node, java.util.Set<Node>>();
    
    // Initialize: entry node dominates only itself
    dominators.put(entry, new java.util.HashSet<>());
    dominators.get(entry).add(entry);
    
    // All other nodes are initially dominated by all nodes
    for (var node : nodes) {
      if (!node.equals(entry)) {
        dominators.put(node, new java.util.HashSet<>(nodes));
      }
    }
    
    // Iterative algorithm to compute dominators
    boolean changed = true;
    while (changed) {
      changed = false;
      
      for (var node : nodes) {
        if (node.equals(entry)) {
          continue;
        }
        
        // New dominator set = {node} ∪ (intersection of dominators of all predecessors)
        var newDominators = new java.util.HashSet<Node>();
        newDominators.add(node);
        
        var predecessors = graph.getPredecessors(node);
        if (!predecessors.isEmpty()) {
          // Start with dominators of first predecessor
          var predIter = predecessors.iterator();
          if (predIter.hasNext()) {
            newDominators.addAll(dominators.get(predIter.next()));
          }
          
          // Intersect with dominators of remaining predecessors
          while (predIter.hasNext()) {
            newDominators.retainAll(dominators.get(predIter.next()));
          }
          
          // Add the node itself
          newDominators.add(node);
        }
        
        // Check if dominator set changed
        if (!newDominators.equals(dominators.get(node))) {
          dominators.put(node, newDominators);
          changed = true;
        }
      }
    }
    
    // Build the dominator tree
    var dominatorTree = new ProgramGraph();
    for (var node : nodes) {
      dominatorTree.addNode(node);
    }

    // Map each node to its immediate dominator
    var immediateDominators = new java.util.HashMap<Node, Node>();
    for (var node : nodes) {
      if (node.equals(entry)) continue;
      var nodeDominators = dominators.get(node);
      var immDom = findImmediateDominator(node, nodeDominators, dominators);
      if (immDom != null) {
        immediateDominators.put(node, immDom);
      }
    }

    // For each dominator, add edges to all nodes it immediately dominates
    for (var node : nodes) {
      for (var entrySet : immediateDominators.entrySet()) {
        if (entrySet.getValue().equals(node)) {
          dominatorTree.addEdge(node, entrySet.getKey());
        }
      }
    }
    return dominatorTree;
  }

  /**
   * Finds the immediate dominator of a node from its dominator set.
   * @param node The node to find immediate dominator for
   * @param dominators The set of all dominators of the node
   * @param allDominators The map of all nodes to their dominator sets
   * @return The immediate dominator, or null if none found
   */
  private Node findImmediateDominator(Node node, java.util.Set<Node> dominators, java.util.Map<Node, java.util.Set<Node>> allDominators) {
    var candidates = new java.util.HashSet<>(dominators);
    candidates.remove(node);
    Node immDom = null;
    for (var candidate : candidates) {
      boolean isImm = true;
      for (var other : candidates) {
        if (!candidate.equals(other) && allDominators.get(other).contains(candidate)) {
          isImm = false;
          break;
        }
      }
      if (isImm) {
        immDom = candidate;
        break;
      }
    }
    return immDom;
  }
  
  /**
   * Checks if one node dominates another based on the dominator sets.
   */
  private boolean dominates(Node dominator,Node dominated,java.util.Set<Node> dominatorSet) {
    return dominatorSet.contains(dominator);
  }
}
