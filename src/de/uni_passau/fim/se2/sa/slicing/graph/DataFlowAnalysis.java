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
    Collection<Variable> usedVariables = new ArrayList<>();
    
    int opcode = pInstruction.getOpcode();
    
    // Handle local variable load instructions
    if (pInstruction instanceof VarInsnNode) {
      VarInsnNode varInsn = (VarInsnNode) pInstruction;
      if (isLoadInstruction(opcode)) {
        Type type = getLocalVariableType(pMethodNode, varInsn.var, pInstruction);
        if (type != null) {
          usedVariables.add(new LocalVariableImpl(type, varInsn.var));
        }
      }
      // IINC both uses and defines a variable
      else if (opcode == Opcodes.IINC) {
        usedVariables.add(new LocalVariableImpl(Type.INT_TYPE, varInsn.var));
      }
    }
    // Handle field access instructions (GETFIELD, GETSTATIC)
    else if (pInstruction instanceof FieldInsnNode) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
        Type fieldType = Type.getType(fieldInsn.desc);
        usedVariables.add(new FieldVariableImpl(fieldType, fieldInsn.owner, fieldInsn.name));
        
        // For non-static field access, the object reference is also used
        if (opcode == Opcodes.GETFIELD) {
          // Note: We don't have direct access to the stack variable, but we know it's used
        }
      }
    }
    // Handle increment instruction
    else if (pInstruction instanceof IincInsnNode) {
      IincInsnNode iincInsn = (IincInsnNode) pInstruction;
      usedVariables.add(new LocalVariableImpl(Type.INT_TYPE, iincInsn.var));
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
  static Collection<Variable> definedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    Collection<Variable> definedVariables = new ArrayList<>();
    
    int opcode = pInstruction.getOpcode();
    
    // Handle local variable store instructions
    if (pInstruction instanceof VarInsnNode) {
      VarInsnNode varInsn = (VarInsnNode) pInstruction;
      if (isStoreInstruction(opcode)) {
        Type type = getLocalVariableType(pMethodNode, varInsn.var, pInstruction);
        if (type != null) {
          definedVariables.add(new LocalVariableImpl(type, varInsn.var));
        }
      }
    }
    // Handle field assignment instructions (PUTFIELD, PUTSTATIC)
    else if (pInstruction instanceof FieldInsnNode) {
      FieldInsnNode fieldInsn = (FieldInsnNode) pInstruction;
      if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
        Type fieldType = Type.getType(fieldInsn.desc);
        definedVariables.add(new FieldVariableImpl(fieldType, fieldInsn.owner, fieldInsn.name));
      }
    }
    // Handle increment instruction (both uses and defines)
    else if (pInstruction instanceof IincInsnNode) {
      IincInsnNode iincInsn = (IincInsnNode) pInstruction;
      definedVariables.add(new LocalVariableImpl(Type.INT_TYPE, iincInsn.var));
    }
    
    return definedVariables;
  }
  
  /**
   * Checks if the given opcode represents a load instruction.
   */
  private static boolean isLoadInstruction(int opcode) {
    return opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD ||
           opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || 
           opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD || opcode == Opcodes.ALOAD;
  }
  
  /**
   * Checks if the given opcode represents a store instruction.
   */
  private static boolean isStoreInstruction(int opcode) {
    return opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE ||
           opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE || 
           opcode == Opcodes.FSTORE || opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE;
  }
  
  /**
   * Gets the type of a local variable at a given instruction.
   */
  private static Type getLocalVariableType(MethodNode methodNode, int varIndex, AbstractInsnNode instruction) {
    // For now, return a generic type based on the instruction opcode
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
    
    return Type.getObjectType("java/lang/Object"); // Default fallback
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
