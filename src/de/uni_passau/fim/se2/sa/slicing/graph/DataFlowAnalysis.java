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
          Variable var = createVariable(varInsn.var);
          if (var != null) {
            used.add(var);
          }
          break;
        default:
          break;
      }
    }
    
    // Handle array loads (uses arrayref and index)
    switch (opcode) {
      case org.objectweb.asm.Opcodes.IALOAD:
      case org.objectweb.asm.Opcodes.LALOAD:
      case org.objectweb.asm.Opcodes.FALOAD:
      case org.objectweb.asm.Opcodes.DALOAD:
      case org.objectweb.asm.Opcodes.AALOAD:
      case org.objectweb.asm.Opcodes.BALOAD:
      case org.objectweb.asm.Opcodes.CALOAD:
      case org.objectweb.asm.Opcodes.SALOAD:
        // Array loads use the array reference and index from the stack
        // For simplicity, we'll create variables for common stack positions
        Variable arrayRef = createVariable(-1); // Use -1 to indicate stack variable
        Variable index = createVariable(-2);
        if (arrayRef != null) used.add(arrayRef);
        if (index != null) used.add(index);
        break;
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
          Variable var = createVariable(varInsn.var);
          if (var != null) {
            defined.add(var);
          }
          break;
        default:
          break;
      }
    }
    
    // Handle array stores
    switch (opcode) {
      case org.objectweb.asm.Opcodes.IASTORE:
      case org.objectweb.asm.Opcodes.LASTORE:
      case org.objectweb.asm.Opcodes.FASTORE:
      case org.objectweb.asm.Opcodes.DASTORE:
      case org.objectweb.asm.Opcodes.AASTORE:
      case org.objectweb.asm.Opcodes.BASTORE:
      case org.objectweb.asm.Opcodes.CASTORE:
      case org.objectweb.asm.Opcodes.SASTORE:
        // Array stores modify the array
        Variable arrayRef = createVariable(-1);
        if (arrayRef != null) defined.add(arrayRef);
        break;
    }
    
    return defined;
  }

  /**
   * Helper method to create Variable instances using reflection.
   */
  private static Variable createVariable(int index) {
    try {
      java.lang.reflect.Method valueOf = Variable.class.getMethod("valueOf", int.class);
      return (Variable) valueOf.invoke(null, index);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create Variable instance for index " + index, e);
    }
  }
}
