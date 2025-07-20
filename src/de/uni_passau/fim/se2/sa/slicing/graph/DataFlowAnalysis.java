package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import br.usp.each.saeg.asm.defuse.VariableImpl;
import java.util.ArrayList;
import java.util.Collection;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MethodInsnNode;
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
    
    switch (pInstruction.getOpcode()) {
      // Handle variable loading instructions
      case Opcodes.ILOAD:
      case Opcodes.LLOAD:
      case Opcodes.FLOAD:
      case Opcodes.DLOAD:
      case Opcodes.ALOAD:
        // int varIndex = ((VarInsnNode) pInstruction).var;
        usedVariables.add(new VariableImpl(getType(pInstruction.getOpcode())));
        break;
        
      // Handle field access (GETFIELD)
      case Opcodes.GETFIELD:
        FieldInsnNode fieldNode = (FieldInsnNode) pInstruction;
        // The object whose field is being accessed is used
        usedVariables.add(new VariableImpl(Type.getObjectType(fieldNode.owner)));
        break;
        
      // Handle IINC instruction (both var and increment value are used)
      case Opcodes.IINC:
        // IincInsnNode iincNode = (IincInsnNode) pInstruction;
        usedVariables.add(new VariableImpl(Type.INT_TYPE));
        break;
        
      // Handle method invocation
      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEINTERFACE:
        // Method parameters are used (including 'this' for non-static methods)
        Type[] argTypes = Type.getArgumentTypes(((MethodInsnNode) pInstruction).desc);
        if (pInstruction.getOpcode() != Opcodes.INVOKESTATIC) {
          // Add 'this' reference for non-static methods
          usedVariables.add(new VariableImpl(Type.getObjectType(pOwningClass)));
        }
        // Add parameters
        for (int i = 0; i < argTypes.length; i++) {
          usedVariables.add(new VariableImpl(argTypes[i]));
        }
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
    
    switch (pInstruction.getOpcode()) {
      // Handle variable storing instructions
      case Opcodes.ISTORE:
      case Opcodes.LSTORE:
      case Opcodes.FSTORE:
      case Opcodes.DSTORE:
      case Opcodes.ASTORE:
        // int varIndex = ((VarInsnNode) pInstruction).var;
        definedVariables.add(new VariableImpl(getType(pInstruction.getOpcode())));
        break;
        
      // Handle field access (PUTFIELD)
      case Opcodes.PUTFIELD:
        FieldInsnNode fieldNode = (FieldInsnNode) pInstruction;
        // The field being stored to is defined
        definedVariables.add(new VariableImpl(Type.getObjectType(fieldNode.owner)));
        break;
        
      // Handle IINC instruction (the variable is defined)
      case Opcodes.IINC:
        // IincInsnNode iincNode = (IincInsnNode) pInstruction;
        definedVariables.add(new VariableImpl(Type.INT_TYPE));
        break;
        
      // Handle method invocation that returns a value
      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEINTERFACE:
        Type returnType = Type.getReturnType(((MethodInsnNode) pInstruction).desc);
        if (returnType != Type.VOID_TYPE) {
          // The return value is defined (stored in the stack, not a local variable)
          // For simplicity, we'll represent it as a special variable
          definedVariables.add(new VariableImpl(returnType)); // stack value
        }
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