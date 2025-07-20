package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import br.usp.each.saeg.asm.defuse.VariableImpl;
import java.util.ArrayList;
import java.util.Collection;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.*;
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
        
      // Handle ILOAD_0 (26), ILOAD_1 (27), ILOAD_2 (28), ILOAD_3 (29)
      case 26: case 27: case 28: case 29:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 26, Type.INT_TYPE));
        break;
      // Handle LLOAD_0 (30), LLOAD_1 (31), LLOAD_2 (32), LLOAD_3 (33)
      case 30: case 31: case 32: case 33:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 30, Type.LONG_TYPE));
        break;
      // Handle FLOAD_0 (34), FLOAD_1 (35), FLOAD_2 (36), FLOAD_3 (37)
      case 34: case 35: case 36: case 37:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 34, Type.FLOAT_TYPE));
        break;
      // Handle DLOAD_0 (38), DLOAD_1 (39), DLOAD_2 (40), DLOAD_3 (41)
      case 38: case 39: case 40: case 41:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 38, Type.DOUBLE_TYPE));
        break;
      // Handle ALOAD_0 (42), ALOAD_1 (43), ALOAD_2 (44), ALOAD_3 (45)
      case 42: case 43: case 44: case 45:
        usedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 42, Type.getType("Ljava/lang/Object;")));
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
        
      // Handle ISTORE_0 (59), ISTORE_1 (60), ISTORE_2 (61), ISTORE_3 (62)
      case 59: case 60: case 61: case 62:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 59, Type.INT_TYPE));
        break;
      // Handle LSTORE_0 (63), LSTORE_1 (64), LSTORE_2 (65), LSTORE_3 (66)
      case 63: case 64: case 65: case 66:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 63, Type.LONG_TYPE));
        break;
      // Handle FSTORE_0 (67), FSTORE_1 (68), FSTORE_2 (69), FSTORE_3 (70)
      case 67: case 68: case 69: case 70:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 67, Type.FLOAT_TYPE));
        break;
      // Handle DSTORE_0 (71), DSTORE_1 (72), DSTORE_2 (73), DSTORE_3 (74)
      case 71: case 72: case 73: case 74:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 71, Type.DOUBLE_TYPE));
        break;
      // Handle ASTORE_0 (75), ASTORE_1 (76), ASTORE_2 (77), ASTORE_3 (78)
      case 75: case 76: case 77: case 78:
        definedVariables.add(new IndexedVariable(pInstruction.getOpcode() - 75, Type.getType("Ljava/lang/Object;")));
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