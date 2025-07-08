package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class DataDependenceGraph extends Graph {

  DataDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  /**
   * Computes the data-dependence graph from the control-flow graph.
   *
   * <p>This requires the computation of the reaching-definition algorithm. We recommend using the
   * provided {@link DataFlowAnalysis} implementation.
   *
   * <p>Remember that the CFG stores for each node the instruction at that node. With that, calling
   * {@link DataFlowAnalysis#definedBy(String, MethodNode, AbstractInsnNode)} provides a collection
   * of {@link Variable}s that are defined by this particular instruction; calling {@link
   * DataFlowAnalysis#usedBy(String, MethodNode, AbstractInsnNode)} provides a collection of {@link
   * Variable}s that are used by this particular instruction, respectively. From this information
   * you can compute for each node n in the CFG the GEN[n] and KILL[n] sets. Afterwards, it is
   * possible to compute the IN[n] and OUT[n] sets using the reaching-definitions algorithm.
   *
   * <p>Finally, you can compute all def-use pairs and construct the data-dependence graph from
   * these pairs.
   *
   * @return The data-dependence graph for a control-flow graph
   */
  @Override
  public ProgramGraph computeResult() {
    // Step 1: Collect all nodes
    ProgramGraph cfg = getCFG(); // Assume Graph superclass provides this
    ProgramGraph ddg = new ProgramGraph();
    for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : cfg.getNodes()) {
      ddg.addNode(node);
    }

    // Step 2: Compute GEN and KILL sets for each node
    java.util.Map<de.uni_passau.fim.se2.sa.slicing.cfg.Node, java.util.Set<String>> gen = new java.util.HashMap<>();
    java.util.Map<de.uni_passau.fim.se2.sa.slicing.cfg.Node, java.util.Set<String>> kill = new java.util.HashMap<>();
    java.util.Map<String, java.util.Set<de.uni_passau.fim.se2.sa.slicing.cfg.Node>> defSites = new java.util.HashMap<>();
    String className = classNode.name;
    for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : cfg.getNodes()) {
      java.util.Set<String> definedVars = new java.util.HashSet<>();
      try {
        for (Variable v : DataFlowAnalysis.definedBy(className, methodNode, node.getInstruction())) {
          definedVars.add(v.toString());
          defSites.computeIfAbsent(v.toString(), k -> new java.util.HashSet<>()).add(node);
        }
      } catch (org.objectweb.asm.tree.analysis.AnalyzerException e) {
        throw new RuntimeException(e);
      }
      gen.put(node, definedVars);
    }
    for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : cfg.getNodes()) {
      java.util.Set<String> killed = new java.util.HashSet<>();
      for (String v : gen.get(node)) {
        for (de.uni_passau.fim.se2.sa.slicing.cfg.Node n2 : cfg.getNodes()) {
          if (n2 != node && gen.get(n2).contains(v)) {
            killed.add(v);
          }
        }
      }
      kill.put(node, killed);
    }

    // Step 3: Reaching definitions analysis (classic iterative)
    java.util.Map<de.uni_passau.fim.se2.sa.slicing.cfg.Node, java.util.Set<java.util.Map.Entry<String, de.uni_passau.fim.se2.sa.slicing.cfg.Node>>> in = new java.util.HashMap<>();
    java.util.Map<de.uni_passau.fim.se2.sa.slicing.cfg.Node, java.util.Set<java.util.Map.Entry<String, de.uni_passau.fim.se2.sa.slicing.cfg.Node>>> out = new java.util.HashMap<>();
    for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : cfg.getNodes()) {
      in.put(node, new java.util.HashSet<>());
      out.put(node, new java.util.HashSet<>());
    }
    boolean changed;
    do {
      changed = false;
      for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : cfg.getNodes()) {
        // IN[n] = union of OUT[p] for all predecessors p
        java.util.Set<java.util.Map.Entry<String, de.uni_passau.fim.se2.sa.slicing.cfg.Node>> inSet = new java.util.HashSet<>();
        for (de.uni_passau.fim.se2.sa.slicing.cfg.Node pred : cfg.getPredecessors(node)) {
          inSet.addAll(out.get(pred));
        }
        // OUT[n] = GEN[n] (from this node) + (IN[n] minus killed)
        java.util.Set<java.util.Map.Entry<String, de.uni_passau.fim.se2.sa.slicing.cfg.Node>> outSet = new java.util.HashSet<>();
        // Add GEN[n]
        for (String v : gen.get(node)) {
          outSet.add(new java.util.AbstractMap.SimpleEntry<>(v, node));
        }
        // Add IN[n] minus killed
        for (java.util.Map.Entry<String, de.uni_passau.fim.se2.sa.slicing.cfg.Node> entry : inSet) {
          if (!kill.get(node).contains(entry.getKey())) {
            outSet.add(entry);
          }
        }
        if (!in.get(node).equals(inSet) || !out.get(node).equals(outSet)) {
          in.put(node, inSet);
          out.put(node, outSet);
          changed = true;
        }
      }
    } while (changed);

    // Step 4: For each use, add data-dependence edges from all reaching definitions
    for (de.uni_passau.fim.se2.sa.slicing.cfg.Node node : cfg.getNodes()) {
      java.util.Set<String> usedVars = new java.util.HashSet<>();
      try {
        for (Variable v : DataFlowAnalysis.usedBy(className, methodNode, node.getInstruction())) {
          usedVars.add(v.toString());
        }
      } catch (org.objectweb.asm.tree.analysis.AnalyzerException e) {
        throw new RuntimeException(e);
      }
      for (String v : usedVars) {
        for (java.util.Map.Entry<String, de.uni_passau.fim.se2.sa.slicing.cfg.Node> def : in.get(node)) {
          if (def.getKey().equals(v)) {
            ddg.addEdge(def.getValue(), node);
          }
        }
      }
    }
    return ddg;
  }
}
