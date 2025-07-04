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
          try {
            // Try common Variable creation patterns
            Variable var = Variable.class.getConstructor(int.class).newInstance(varInsn.var);
            used.add(var);
          } catch (Exception e) {
            // If constructor doesn't work, try other approaches
            try {
              java.lang.reflect.Method valueOf = Variable.class.getMethod("valueOf", int.class);
              Variable var = (Variable) valueOf.invoke(null, varInsn.var);
              used.add(var);
            } catch (Exception e2) {
              // If all else fails, create via reflection
              try {
                java.lang.reflect.Constructor<?>[] constructors = Variable.class.getDeclaredConstructors();
                for (java.lang.reflect.Constructor<?> constructor : constructors) {
                  constructor.setAccessible(true);
                  if (constructor.getParameterCount() == 1) {
                    Variable var = (Variable) constructor.newInstance(varInsn.var);
                    used.add(var);
                    break;
                  }
                }
              } catch (Exception e3) {
                // Last resort - skip this variable
              }
            }
          }
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
          try {
            // Try common Variable creation patterns
            Variable var = Variable.class.getConstructor(int.class).newInstance(varInsn.var);
            defined.add(var);
          } catch (Exception e) {
            // If constructor doesn't work, try other approaches
            try {
              java.lang.reflect.Method valueOf = Variable.class.getMethod("valueOf", int.class);
              Variable var = (Variable) valueOf.invoke(null, varInsn.var);
              defined.add(var);
            } catch (Exception e2) {
              // If all else fails, create via reflection
              try {
                java.lang.reflect.Constructor<?>[] constructors = Variable.class.getDeclaredConstructors();
                for (java.lang.reflect.Constructor<?> constructor : constructors) {
                  constructor.setAccessible(true);
                  if (constructor.getParameterCount() == 1) {
                    Variable var = (Variable) constructor.newInstance(varInsn.var);
                    defined.add(var);
                    break;
                  }
                }
              } catch (Exception e3) {
                // Last resort - skip this variable
              }
            }
          }
          break;
        default:
          break;
      }
    }
    return defined;
  }
}
