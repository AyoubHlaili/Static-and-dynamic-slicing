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
    if (pInstruction == null) {
      return usedVariables;
    }
    int opcode = pInstruction.getOpcode();
    
    // Local variable load instructions - use local variables
    if (pInstruction instanceof VarInsnNode && 
        (opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || opcode == Opcodes.FLOAD ||
         opcode == Opcodes.DLOAD || opcode == Opcodes.ALOAD)) {
      VarInsnNode varInsn = (VarInsnNode) pInstruction;
      Type type = getLocalVariableType(pMethodNode, varInsn.var, pInstruction);
      usedVariables.add(new VariableImpl(type));
    }
    
    // Increment instruction - uses local variable
    if (pInstruction instanceof IincInsnNode) {
      IincInsnNode iincInsn = (IincInsnNode) pInstruction;
      Type type = getLocalVariableType(pMethodNode, iincInsn.var, pInstruction);
      usedVariables.add(new VariableImpl(type));
    }
    
    // Field instructions use the field
    if (pInstruction instanceof FieldInsnNode && 
        (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC)) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      Type fieldType = Type.getType(fieldInsn.desc);
      usedVariables.add(new VariableImpl(fieldType));
    }
    
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
    if (pInstruction == null) {
      return definedVariables;
    }
    
    // Check if this is the first instruction (method entry) - parameters are defined here
    boolean isFirstInstruction = pMethodNode.instructions.getFirst() == pInstruction;
    if (isFirstInstruction) {
      // Add method parameters as defined variables
      Type[] argumentTypes = Type.getArgumentTypes(pMethodNode.desc);
      
      // For non-static methods, 'this' is parameter 0
      if ((pMethodNode.access & Opcodes.ACC_STATIC) == 0) {
        Type thisType = Type.getObjectType(pOwningClass);
        definedVariables.add(new VariableImpl(thisType));
      }
      
      // Add actual method parameters
      for (Type argType : argumentTypes) {
        definedVariables.add(new VariableImpl(argType));
      }
    }
    
    int opcode = pInstruction.getOpcode();
    
    // Local variable store instructions - define local variables
    if (pInstruction instanceof VarInsnNode && 
        (opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE || opcode == Opcodes.FSTORE ||
         opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE)) {
      VarInsnNode varInsn = (VarInsnNode) pInstruction;
      Type type = getLocalVariableType(pMethodNode, varInsn.var, pInstruction);
      definedVariables.add(new VariableImpl(type));
    }
    
    // Increment instruction - defines local variable
    if (pInstruction instanceof IincInsnNode) {
      IincInsnNode iincInsn = (IincInsnNode) pInstruction;
      Type type = getLocalVariableType(pMethodNode, iincInsn.var, pInstruction);
      definedVariables.add(new VariableImpl(type));
    }
    
    // Field instructions define the field
    if (pInstruction instanceof FieldInsnNode && 
        (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      Type fieldType = Type.getType(fieldInsn.desc);
      definedVariables.add(new VariableImpl(fieldType));
    }
    
    // Method parameters are implicitly defined at method entry
    // This would need special handling for the first instruction
    
    // Array store instructions could define array elements
    // But this requires more complex analysis to track array references
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