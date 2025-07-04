package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import java.util.Collection;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/** Provides a simple data-flow analysis. */
class DataFlowAnalysis {

  private DataFlowAnalysis() {}

  /**
   * Provides the collection of {@link Variable}s that are used by the given instruction.
   *
   * @param pOwningClass The class that owns the method
   * @param pMethodNode The method that contains the instruction
   * @param pInstruction The instruction
   * @return The collection of {@link Variable}s that are used by the given instruction
   * @throws AnalyzerException In case an error occurs during the analysis
   */
  static Collection<Variable> usedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    java.util.List<Variable> used = new java.util.ArrayList<>();
    int opcode = pInstruction.getOpcode();
    if (opcode == -1) return used; // Not an instruction

    // Handle local variable loads
    if (pInstruction instanceof org.objectweb.asm.tree.VarInsnNode) {
      org.objectweb.asm.tree.VarInsnNode varInsn = (org.objectweb.asm.tree.VarInsnNode) pInstruction;
      switch (opcode) {
        case org.objectweb.asm.Opcodes.ILOAD:
        case org.objectweb.asm.Opcodes.LLOAD:
        case org.objectweb.asm.Opcodes.FLOAD:
        case org.objectweb.asm.Opcodes.DLOAD:
        case org.objectweb.asm.Opcodes.ALOAD:
          used.add((Variable) (Object) Integer.valueOf(varInsn.var)); // fallback: use index as variable
          break;
        default:
          break;
      }
    }
    return used;
  }

  /**
   * Provides the collection of {@link Variable}s that are defined by the given instruction.
   *
   * @param pOwningClass The class that owns the method
   * @param pMethodNode The method that contains the instruction
   * @param pInstruction The instruction
   * @return The collection of {@link Variable}s that are defined by the given instruction
   * @throws AnalyzerException In case an error occurs during the analysis
   */
  static Collection<Variable> definedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    java.util.List<Variable> defined = new java.util.ArrayList<>();
    int opcode = pInstruction.getOpcode();
    if (opcode == -1) return defined; // Not an instruction

    // Handle local variable stores
    if (pInstruction instanceof org.objectweb.asm.tree.VarInsnNode) {
      org.objectweb.asm.tree.VarInsnNode varInsn = (org.objectweb.asm.tree.VarInsnNode) pInstruction;
      switch (opcode) {
        case org.objectweb.asm.Opcodes.ISTORE:
        case org.objectweb.asm.Opcodes.LSTORE:
        case org.objectweb.asm.Opcodes.FSTORE:
        case org.objectweb.asm.Opcodes.DSTORE:
        case org.objectweb.asm.Opcodes.ASTORE:
          defined.add((Variable) (Object) Integer.valueOf(varInsn.var)); // fallback: use index as variable
          break;
        default:
          break;
      }
    }
    return defined;
  }
}
