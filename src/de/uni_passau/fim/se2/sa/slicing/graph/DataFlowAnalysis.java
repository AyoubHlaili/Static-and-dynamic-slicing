package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import br.usp.each.saeg.asm.defuse.VariableImpl;
import java.util.ArrayList;
import java.util.Collection;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/** Provides a simple data-flow analysis. */
public class DataFlowAnalysis {

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
  public static Collection<Variable> usedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction
  ) throws AnalyzerException {
    Collection<Variable> usedVariables = new ArrayList<>();
    
    int opcode = pInstruction.getOpcode();
    // Local variable is used by load and iinc instructions
    if (opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || opcode == Opcodes.FLOAD ||
        opcode == Opcodes.DLOAD || opcode == Opcodes.ALOAD || opcode == Opcodes.IINC) {
      int varIdx;
      if (pInstruction instanceof VarInsnNode) {
        varIdx = ((VarInsnNode) pInstruction).var;
      } else if (pInstruction instanceof IincInsnNode) {
        varIdx = ((IincInsnNode) pInstruction).var;
      } else {
        varIdx = -1;
      }
      if (varIdx >= 0) {
        Type type = getLocalVariableType(pMethodNode, varIdx, pInstruction);
        usedVariables.add(new VariableImpl(type));
      }
    }
    // Field instructions use the field
    if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      Type fieldType = Type.getType(fieldInsn.desc);
      usedVariables.add(new VariableImpl(fieldType));
    }
    // (Array loads, method calls, etc. can be added as needed)
    return usedVariables;
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
  public static Collection<Variable> definedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction
  ) throws AnalyzerException {
    Collection<Variable> definedVariables = new ArrayList<>();
    
    int opcode = pInstruction.getOpcode();
    // Local variable is defined by store and iinc instructions
    if (opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE || opcode == Opcodes.FSTORE ||
        opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE || opcode == Opcodes.IINC) {
      int varIdx;
      if (pInstruction instanceof VarInsnNode) {
        varIdx = ((VarInsnNode) pInstruction).var;
      } else if (pInstruction instanceof IincInsnNode) {
        varIdx = ((IincInsnNode) pInstruction).var;
      } else {
        varIdx = -1;
      }
      if (varIdx >= 0) {
        Type type = getLocalVariableType(pMethodNode, varIdx, pInstruction);
        definedVariables.add(new VariableImpl(type));
      }
    }
    // Field instructions define the field
    if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      Type fieldType = Type.getType(fieldInsn.desc);
      definedVariables.add(new VariableImpl(fieldType));
    }
    // (Array stores, etc. can be added as needed)
    return definedVariables;
  }
  
  /**
   * Helper method to determine the type of a local variable at a specific instruction.
   * This is a simplified implementation that infers types based on instruction opcodes.
   *
   * @param pMethodNode The method containing the variable
   * @param varIndex The local variable index
   * @param pInstruction The instruction context
   * @return The type of the local variable
   */
  private static Type getLocalVariableType(MethodNode pMethodNode, int varIndex, AbstractInsnNode pInstruction) {
    // Check if we can determine type from local variable table
    if (pMethodNode.localVariables != null) {
      for (Object localVar : pMethodNode.localVariables) {
        org.objectweb.asm.tree.LocalVariableNode lvn = (org.objectweb.asm.tree.LocalVariableNode) localVar;
        if (lvn.index == varIndex) {
          return Type.getType(lvn.desc);
        }
      }
    }
    
    // Fallback: infer type from instruction opcode
    switch (pInstruction.getOpcode()) {
      case Opcodes.ILOAD:
      case Opcodes.ISTORE:
      case Opcodes.IINC:
        return Type.INT_TYPE;
      case Opcodes.LLOAD:
      case Opcodes.LSTORE:
        return Type.LONG_TYPE;
      case Opcodes.FLOAD:
      case Opcodes.FSTORE:
        return Type.FLOAT_TYPE;
      case Opcodes.DLOAD:
      case Opcodes.DSTORE:
        return Type.DOUBLE_TYPE;
      case Opcodes.ALOAD:
      case Opcodes.ASTORE:
        return Type.getObjectType("java/lang/Object");
      default:
        return Type.getObjectType("java/lang/Object");
    }
  }
}