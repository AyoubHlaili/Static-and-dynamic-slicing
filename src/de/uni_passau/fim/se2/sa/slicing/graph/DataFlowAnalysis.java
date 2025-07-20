package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseFrame;
import br.usp.each.saeg.asm.defuse.Variable;
import java.util.Collection;
import java.util.Collections;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/**
 * Performs data flow analysis to identify variable definitions and uses.
 * This implementation leverages the DefUseAnalyzer for accurate analysis.
 */
final class DataFlowAnalysis {

    // Private constructor to prevent instantiation
    private DataFlowAnalysis() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Identifies variables used by a specific instruction.
     *
     * @param ownerClass The containing class name
     * @param method The method being analyzed
     * @param instruction The target instruction
     * @return Collection of used variables
     * @throws AnalyzerException If analysis fails
     */
    static Collection<Variable> usedBy(
            String ownerClass, MethodNode method, AbstractInsnNode instruction) 
            throws AnalyzerException {
        
        return performAnalysis(ownerClass, method, instruction, AnalysisType.USE);
    }

    /**
     * Identifies variables defined by a specific instruction.
     *
     * @param ownerClass The containing class name
     * @param method The method being analyzed
     * @param instruction The target instruction
     * @return Collection of defined variables
     * @throws AnalyzerException If analysis fails
     */
    static Collection<Variable> definedBy(
            String ownerClass, MethodNode method, AbstractInsnNode instruction) 
            throws AnalyzerException {
        
        return performAnalysis(ownerClass, method, instruction, AnalysisType.DEFINITION);
    }

    /**
     * Internal analysis method handling both use and definition cases.
     */
    private static Collection<Variable> performAnalysis(
            String ownerClass, 
            MethodNode method, 
            AbstractInsnNode instruction,
            AnalysisType analysisType) throws AnalyzerException {
        
        try {
            DefUseAnalyzer analyzer = new DefUseAnalyzer();
            analyzer.analyze(ownerClass, method);
            
            DefUseFrame[] analysisResults = analyzer.getDefUseFrames();
            int instructionPosition = findInstructionPosition(method, instruction);
            
            if (isValidPosition(instructionPosition, analysisResults)) {
                DefUseFrame frame = analysisResults[instructionPosition];
                return analysisType == AnalysisType.USE ? 
                       frame.getUses() : frame.getDefinitions();
            }
        } catch (Exception e) {
            throw new AnalyzerException(instruction, "Analysis failed", e);
        }
        
        return Collections.emptyList();
    }

    /**
     * Locates the position of an instruction in the method.
     */
    private static int findInstructionPosition(MethodNode method, AbstractInsnNode instruction) {
        int position = 0;
        for (AbstractInsnNode node = method.instructions.getFirst(); 
             node != null; 
             node = node.getNext()) {
            if (node == instruction) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /**
     * Validates if the instruction position is within bounds.
     */
    private static boolean isValidPosition(int position, DefUseFrame[] frames) {
        return position >= 0 && position < frames.length && frames[position] != null;
    }

    /**
     * Enumeration of analysis types.
     */
    private enum AnalysisType {
        USE,
        DEFINITION
    }
}