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
            // Variable loading instructions
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD: {
                usedVariables.add(new VariableImpl(getType(pInstruction.getOpcode())));
                break;
            }
            // Field access instructions
            case Opcodes.GETFIELD:
            case Opcodes.PUTFIELD: {
                FieldInsnNode fieldNode = (FieldInsnNode) pInstruction;
                usedVariables.add(new VariableImpl(Type.getObjectType(fieldNode.owner)));
                break;
            }
            // Increment instruction
            case Opcodes.IINC: {
                usedVariables.add(new VariableImpl(Type.INT_TYPE));
                break;
            }
            // Method invocation instructions
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                Type[] argTypes = Type.getArgumentTypes(((MethodInsnNode) pInstruction).desc);
                if (pInstruction.getOpcode() != Opcodes.INVOKESTATIC) {
                    usedVariables.add(new VariableImpl(Type.getObjectType(pOwningClass)));
                }
                for (Type argType : argTypes) {
                    usedVariables.add(new VariableImpl(argType));
                }
                break;
            }
            // Array load instructions
            case Opcodes.IALOAD:
            case Opcodes.LALOAD:
            case Opcodes.FALOAD:
            case Opcodes.DALOAD:
            case Opcodes.AALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD: {
                usedVariables.add(new VariableImpl(Type.getType("Ljava/lang/Object;")));
                usedVariables.add(new VariableImpl(Type.INT_TYPE));
                break;
            }
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
        
        // Handle method parameters at method start
        if (pMethodNode.instructions.indexOf(pInstruction) == 0) {
            Type[] argTypes = Type.getArgumentTypes(pMethodNode.desc);
            for (Type argType : argTypes) {
                definedVariables.add(new VariableImpl(argType));
            }
            return definedVariables;
        }
        
        // Handle other instructions
        switch (pInstruction.getOpcode()) {
            // Variable storing instructions
            case Opcodes.ISTORE:
            case Opcodes.LSTORE:
            case Opcodes.FSTORE:
            case Opcodes.DSTORE:
            case Opcodes.ASTORE: {
                definedVariables.add(new VariableImpl(getType(pInstruction.getOpcode())));
                break;
            }
            // Field access instructions
            case Opcodes.PUTFIELD: {
                FieldInsnNode fieldNode = (FieldInsnNode) pInstruction;
                definedVariables.add(new VariableImpl(Type.getObjectType(fieldNode.owner)));
                break;
            }
            // Increment instruction
            case Opcodes.IINC: {
                definedVariables.add(new VariableImpl(Type.INT_TYPE));
                break;
            }
            // Method invocation instructions with return value
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                Type returnType = Type.getReturnType(((MethodInsnNode) pInstruction).desc);
                if (returnType != Type.VOID_TYPE) {
                    definedVariables.add(new VariableImpl(returnType)); // -1 indicates stack
                }
                break;
            }
            // Array store instructions
            case Opcodes.IASTORE:
            case Opcodes.LASTORE:
            case Opcodes.FASTORE:
            case Opcodes.DASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE: {
                definedVariables.add(new VariableImpl(Type.getType("Ljava/lang/Object;")));
                break;
            }
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