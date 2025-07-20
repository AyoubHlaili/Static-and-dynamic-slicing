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
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    Collection<Variable> usedVariables = new ArrayList<>();
    if (pInstruction == null) {
      return usedVariables;
    }
    
    int opcode = pInstruction.getOpcode();
    
    // Skip pseudo-instructions that don't actually execute
    if (opcode == -1) {
      return usedVariables;
    }
    
    // Handle local variable load instructions
    if (pInstruction instanceof VarInsnNode) {
      VarInsnNode varInsn = (VarInsnNode) pInstruction;
      // Only handle actual load instructions, not stores
      if (opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || 
          opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD || opcode == Opcodes.ALOAD) {
        Type type = getLocalVariableType(pMethodNode, varInsn.var, pInstruction);
        usedVariables.add(new LocalVariableImpl(type, varInsn.var));
      }
    }
    // Handle field access instructions that read fields
    else if (pInstruction instanceof FieldInsnNode) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
        Type fieldType = Type.getType(fieldInsn.desc);
        usedVariables.add(new FieldVariableImpl(fieldType, fieldInsn.owner, fieldInsn.name));
      }
    }
    // Handle increment instruction (reads before incrementing)
    else if (pInstruction instanceof IincInsnNode) {
      IincInsnNode iincInsn = (IincInsnNode) pInstruction;
      usedVariables.add(new LocalVariableImpl(Type.INT_TYPE, iincInsn.var));
    }
    // Explicitly handle array load operations (use array reference and index)
    else if (opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD) {
      // No local variable can be determined without stack analysis
      // Do not add any used variable
    }
    // Explicitly handle array store operations (use array reference, index, and value)
    else if (opcode >= Opcodes.IASTORE && opcode <= Opcodes.SASTORE) {
      // No local variable can be determined without stack analysis
      // Do not add any used variable
    }
    // All other instructions (arithmetic, control flow, etc.) don't use local variables or fields
    
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
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    Collection<Variable> definedVariables = new ArrayList<>();
    if (pInstruction == null) {
      return definedVariables;
    }
    
    int opcode = pInstruction.getOpcode();
    
    // Skip pseudo-instructions that don't actually execute
    if (opcode == -1) {
      return definedVariables;
    }
    
    // Handle local variable store instructions
    if (pInstruction instanceof VarInsnNode) {
      VarInsnNode varInsn = (VarInsnNode) pInstruction;
      // Only handle actual store instructions, not loads
      if (opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE || 
          opcode == Opcodes.FSTORE || opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE) {
        Type type = getLocalVariableType(pMethodNode, varInsn.var, pInstruction);
        definedVariables.add(new LocalVariableImpl(type, varInsn.var));
      }
    }
    // Handle field assignment instructions
    else if (pInstruction instanceof FieldInsnNode) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
        Type fieldType = Type.getType(fieldInsn.desc);
        definedVariables.add(new FieldVariableImpl(fieldType, fieldInsn.owner, fieldInsn.name));
      }
    }
    // Handle increment instruction (defines the variable after incrementing)
    else if (pInstruction instanceof IincInsnNode) {
      IincInsnNode iincInsn = (IincInsnNode) pInstruction;
      definedVariables.add(new LocalVariableImpl(Type.INT_TYPE, iincInsn.var));
    }
    // Explicitly handle array load operations (do not define any local variable)
    else if (opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD) {
      // Array loads do not define a local variable directly
    }
    // Explicitly handle array store operations (do not define local variables)
    else if (opcode >= Opcodes.IASTORE && opcode <= Opcodes.SASTORE) {
      // Array stores don't define local variables, they define array elements
    }
    // All other instructions don't define local variables or fields
    
    return definedVariables;
  }
  
  /**
   * Gets the type of a local variable at a given instruction.
   */
  private static Type getLocalVariableType(MethodNode methodNode, int varIndex, AbstractInsnNode instruction) {
    // First try to get type from local variable table if available
    if (methodNode.localVariables != null) {
      for (org.objectweb.asm.tree.LocalVariableNode lvn : methodNode.localVariables) {
        if (lvn.index == varIndex) {
          return Type.getType(lvn.desc);
        }
      }
    }
    
    // Fallback to type inference based on instruction opcode
    int opcode = instruction.getOpcode();
    
    if (opcode == Opcodes.ILOAD || opcode == Opcodes.ISTORE || opcode == Opcodes.IINC) {
      return Type.INT_TYPE;
    } else if (opcode == Opcodes.LLOAD || opcode == Opcodes.LSTORE) {
      return Type.LONG_TYPE;
    } else if (opcode == Opcodes.FLOAD || opcode == Opcodes.FSTORE) {
      return Type.FLOAT_TYPE;
    } else if (opcode == Opcodes.DLOAD || opcode == Opcodes.DSTORE) {
      return Type.DOUBLE_TYPE;
    } else if (opcode == Opcodes.ALOAD || opcode == Opcodes.ASTORE) {
      return Type.getObjectType("java/lang/Object");
    }
    
    // Final fallback - if we can't determine the type, still return a default
    // This ensures we don't lose variable definitions due to type inference failure
    return Type.getObjectType("java/lang/Object");
  }
  
  /**
   * Implementation of Variable for local variables.
   */
  private static class LocalVariableImpl extends VariableImpl {
    private final int index;
    private final Type type;
    
    public LocalVariableImpl(Type type, int index) {
      super(type);
      this.type = type;
      this.index = index;
    }
    
    @Override
    public String toString() {
      return "LocalVar[" + index + ":" + type + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof LocalVariableImpl)) return false;
      LocalVariableImpl other = (LocalVariableImpl) obj;
      return index == other.index && type.equals(other.type);
    }
    
    @Override
    public int hashCode() {
      return java.util.Objects.hash(index, type);
    }
  }
  
  /**
   * Implementation of Variable for field variables.
   */
  private static class FieldVariableImpl extends VariableImpl {
    private final String owner;
    private final String name;
    private final Type type;
    
    public FieldVariableImpl(Type type, String owner, String name) {
      super(type);
      this.type = type;
      this.owner = owner;
      this.name = name;
    }
    
    @Override
    public String toString() {
      return "Field[" + owner + "." + name + ":" + type + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof FieldVariableImpl)) return false;
      FieldVariableImpl other = (FieldVariableImpl) obj;
      return owner.equals(other.owner) && name.equals(other.name) && type.equals(other.type);
    }
    
    @Override
    public int hashCode() {
      return java.util.Objects.hash(owner, name, type);
    }
  }
}
