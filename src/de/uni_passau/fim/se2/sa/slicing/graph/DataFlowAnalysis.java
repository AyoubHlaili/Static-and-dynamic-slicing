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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/** Provides a simple data-flow analysis. */
public class DataFlowAnalysis {

  private DataFlowAnalysis() {}
  
  /**
   * A custom variable implementation that tracks variable indices.
   */
  private static class IndexedVariable extends VariableImpl {
    private final int index;
    private final Type type;
    
    public IndexedVariable(int index, Type type) {
      super(type);
      this.index = index;
      this.type = type;
    }
    
    @Override
    public String toString() {
      return "Var[" + index + ":" + type + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof IndexedVariable)) return false;
      IndexedVariable other = (IndexedVariable) obj;
      return index == other.index && type.equals(other.type);
    }
    
    @Override
    public int hashCode() {
      return index * 31 + type.hashCode();
    }
  }

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
    switch (pInstruction.getOpcode()) {
      // Handle variable loading instructions
      case Opcodes.ILOAD:
      case Opcodes.LLOAD:
      case Opcodes.FLOAD:
      case Opcodes.DLOAD:
      case Opcodes.ALOAD:
        VarInsnNode varNode = (VarInsnNode) pInstruction;
        usedVariables.add(new IndexedVariable(varNode.var, getType(pInstruction.getOpcode())));
        break;
        
      // Handle ILOAD_0, ILOAD_1, etc.
      case Opcodes.ILOAD_0: case Opcodes.ILOAD_1: case Opcodes.ILOAD_2: case Opcodes.ILOAD_3:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.ILOAD_0, Type.INT_TYPE));
        break;
      case Opcodes.LLOAD_0: case Opcodes.LLOAD_1: case Opcodes.LLOAD_2: case Opcodes.LLOAD_3:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.LLOAD_0, Type.LONG_TYPE));
        break;
      case Opcodes.FLOAD_0: case Opcodes.FLOAD_1: case Opcodes.FLOAD_2: case Opcodes.FLOAD_3:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.FLOAD_0, Type.FLOAT_TYPE));
        break;
      case Opcodes.DLOAD_0: case Opcodes.DLOAD_1: case Opcodes.DLOAD_2: case Opcodes.DLOAD_3:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.DLOAD_0, Type.DOUBLE_TYPE));
        break;
      case Opcodes.ALOAD_0: case Opcodes.ALOAD_1: case Opcodes.ALOAD_2: case Opcodes.ALOAD_3:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.ALOAD_0, Type.getType("Ljava/lang/Object;")));
        break;
        
      // Handle field access (GETFIELD)
      case Opcodes.GETFIELD:
        FieldInsnNode fieldNode = (FieldInsnNode) pInstruction;
        // The object whose field is being accessed is used (typically 'this' in slot 0)
        usedVariables.add(new IndexedVariable(0, Type.getObjectType(fieldNode.owner)));
        break;
        
      // Handle IINC instruction (both var and increment value are used)
      case Opcodes.IINC:
        IincInsnNode iincNode = (IincInsnNode) pInstruction;
        usedVariables.add(new IndexedVariable(iincNode.var, Type.INT_TYPE));
        break;
        
      // Handle method invocation
      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEINTERFACE:
        // Method parameters are used (including 'this' for non-static methods)
        Type[] argTypes = Type.getArgumentTypes(((MethodInsnNode) pInstruction).desc);
        int paramIndex = 0;
        if (pInstruction.getOpcode() != Opcodes.INVOKESTATIC) {
          // Add 'this' reference for non-static methods
          usedVariables.add(new IndexedVariable(paramIndex++, Type.getObjectType(pOwningClass)));
        }
        // Add parameters
        for (Type argType : argTypes) {
          usedVariables.add(new IndexedVariable(paramIndex++, argType));
        }
        break;
        
      // All other instructions (constants, arithmetic, etc.) don't use local variables
      default:
        // No variables used
        break;
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
    switch (pInstruction.getOpcode()) {
      // Handle variable storing instructions
      case Opcodes.ISTORE:
      case Opcodes.LSTORE:
      case Opcodes.FSTORE:
      case Opcodes.DSTORE:
      case Opcodes.ASTORE:
        VarInsnNode varNode = (VarInsnNode) pInstruction;
        definedVariables.add(new IndexedVariable(varNode.var, getType(pInstruction.getOpcode())));
        break;
        
      // Handle ISTORE_0, ISTORE_1, etc.
      case Opcodes.ISTORE_0: case Opcodes.ISTORE_1: case Opcodes.ISTORE_2: case Opcodes.ISTORE_3:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.ISTORE_0, Type.INT_TYPE));
        break;
      case Opcodes.LSTORE_0: case Opcodes.LSTORE_1: case Opcodes.LSTORE_2: case Opcodes.LSTORE_3:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.LSTORE_0, Type.LONG_TYPE));
        break;
      case Opcodes.FSTORE_0: case Opcodes.FSTORE_1: case Opcodes.FSTORE_2: case Opcodes.FSTORE_3:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.FSTORE_0, Type.FLOAT_TYPE));
        break;
      case Opcodes.DSTORE_0: case Opcodes.DSTORE_1: case Opcodes.DSTORE_2: case Opcodes.DSTORE_3:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.DSTORE_0, Type.DOUBLE_TYPE));
        break;
      case Opcodes.ASTORE_0: case Opcodes.ASTORE_1: case Opcodes.ASTORE_2: case Opcodes.ASTORE_3:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - Opcodes.ASTORE_0, Type.getType("Ljava/lang/Object;")));
        break;
        
      // Handle field access (PUTFIELD)
      case Opcodes.PUTFIELD:
        // The field being stored to is defined (not the local variable index, but the field itself)
        // For simplicity, we don't track field definitions as variable indices
        break;
        
      // Handle IINC instruction (the variable is defined)
      case Opcodes.IINC:
        IincInsnNode iincNode = (IincInsnNode) pInstruction;
        definedVariables.add(new IndexedVariable(iincNode.var, Type.INT_TYPE));
        break;
        
      // Handle method invocation that returns a value
      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEINTERFACE:
        Type returnType = Type.getReturnType(((MethodInsnNode) pInstruction).desc);
        if (returnType != Type.VOID_TYPE) {
          // The return value is defined (stored in the stack, not a local variable)
          // For simplicity, we'll represent it as a special variable with index -1
          definedVariables.add(new IndexedVariable(-1, returnType));
        }
        break;
        
      // All other instructions (constants, arithmetic, etc.) don't define local variables
      default:
        // No variables defined
        break;
    }
    
    return definedVariables;
  }
  
  /**
   * Helper method to get the Type corresponding to an opcode.
   */
  private static Type getType(int opcode) {
    switch (opcode) {
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
        return Type.getType("Ljava/lang/Object;");
      default:
        return Type.VOID_TYPE;
    }
  }
}