package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import java.util.Collection;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.Type;

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
          Variable var = createVariable(pMethodNode, varInsn.var);
          System.out.println("[usedBy] Instruction: " + opcode + ", index: " + varInsn.var + ", Variable: " + var);
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
        Variable arrayRef = createVariable(pMethodNode, -1); // Use -1 to indicate stack variable
        Variable index = createVariable(pMethodNode, -2);
        System.out.println("[usedBy] Array load, index: -1, Variable: " + arrayRef);
        System.out.println("[usedBy] Array load, index: -2, Variable: " + index);
        if (arrayRef != null) used.add(arrayRef);
        if (index != null) used.add(index);
        break;
    }
    System.out.println("Used variables: " + used);
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
          // Always create a variable for store instructions, even if not in local variable table
          Variable var = createVariable(pMethodNode, varInsn.var);
          System.out.println("[definedBy] Instruction: " + opcode + ", index: " + varInsn.var + ", Variable: " + var);
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
        Variable arrayRef = createVariable(pMethodNode, -1);
        System.out.println("[definedBy] Array store, index: -1, Variable: " + arrayRef);
        if (arrayRef != null) defined.add(arrayRef);
        break;
    }
    System.out.println("Defined variables: " + defined);
    return defined;
  }

  /**
   * Helper method to create Variable instances with the correct type from the local variable table.
   */
  private static Variable createVariable(MethodNode methodNode, int index) {
    try {
      org.objectweb.asm.Type type = org.objectweb.asm.Type.INT_TYPE; // default
      boolean found = false;
      // For stack variables (negative indices), always use INT_TYPE
      if (index < 0) {
        found = true;
      } else if (methodNode != null && methodNode.localVariables != null) {
        for (Object obj : methodNode.localVariables) {
          org.objectweb.asm.tree.LocalVariableNode lvn = (org.objectweb.asm.tree.LocalVariableNode) obj;
          if (lvn.index == index) {
            type = org.objectweb.asm.Type.getType(lvn.desc);
            found = true;
            break;
          }
        }
      }
      // If not found and index == 0, try to infer 'this' or first argument
      if (!found && index == 0 && methodNode != null) {
        boolean isStatic = (methodNode.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0;
        if (!isStatic) {
          // 'this' reference: use the class type
          // Try to get the class name from the methodNode's parent (not available here), so fallback to Object
          type = org.objectweb.asm.Type.getType("Ljava/lang/Object;");
          found = true;
        }
      }
      // If still not found, fallback to INT_TYPE
      // (type already set)
      Class<?> variableImplClass = Class.forName("br.usp.each.saeg.asm.defuse.VariableImpl");
      java.lang.reflect.Constructor<?> constructor = variableImplClass.getConstructor(int.class, org.objectweb.asm.Type.class);
      Variable v = (Variable) constructor.newInstance(index, type);
      System.out.println("[createVariable] index: " + index + ", type: " + type + ", Variable: " + v);
      return v;
    } catch (Exception e) {
      System.out.println("[createVariable] ERROR for index: " + index + ", message: " + e.getMessage());
      throw new RuntimeException("Failed to create Variable instance for index " + index, e);
    }
  }
}
