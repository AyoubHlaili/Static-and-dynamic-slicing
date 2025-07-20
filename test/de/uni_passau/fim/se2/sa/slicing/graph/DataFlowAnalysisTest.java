package de.uni_passau.fim.se2.sa.slicing.graph;

import org.junit.jupiter.api.Test;

public class DataFlowAnalysisTest {
    @Test
    void testDefinedBy() throws Exception {
        // Load a sample class and method (adjust as needed)
        String className = "de/uni_passau/fim/se2/sa/examples/SimpleInteger";
        String methodName = "foo";
        String methodDesc = "()I";

        org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(className);
        org.objectweb.asm.tree.ClassNode cn = new org.objectweb.asm.tree.ClassNode();
        cr.accept(cn, 0);
        org.objectweb.asm.tree.MethodNode mn = null;
        for (Object m : cn.methods) {
            org.objectweb.asm.tree.MethodNode mnode = (org.objectweb.asm.tree.MethodNode) m;
            if (mnode.name.equals(methodName) && mnode.desc.equals(methodDesc)) {
                mn = mnode;
                break;
            }
        }
        assert mn != null;
        // Find the first instruction that defines a variable
        org.objectweb.asm.tree.AbstractInsnNode foundInsn = null;
        int foundIndex = -1;
        java.util.Collection<br.usp.each.saeg.asm.defuse.Variable> defined = null;
        for (int i = 0; i < mn.instructions.size(); i++) {
            org.objectweb.asm.tree.AbstractInsnNode insn = mn.instructions.get(i);
            defined = DataFlowAnalysis.definedBy(className, mn, insn);
            if (defined != null && !defined.isEmpty()) {
                foundInsn = insn;
                foundIndex = i;
                break;
            }
        }
        if (foundInsn == null) {
            // Print all instructions to debug why none define variables
            System.out.println("No instruction defines a variable. All instructions:");
            for (int i = 0; i < mn.instructions.size(); i++) {
                org.objectweb.asm.tree.AbstractInsnNode insn = mn.instructions.get(i);
                System.out.println("Index " + i + ": opcode=" + insn.getOpcode() + ", type=" + insn.getClass().getSimpleName());
            }
            throw new AssertionError("No instruction defines a variable in this method");
        }
        System.out.println("First variable-defining instruction at index " + foundIndex + ": opcode=" + foundInsn.getOpcode() + ", type=" + foundInsn.getClass().getSimpleName());
        System.out.println("Defined variables: " + defined);
        org.junit.jupiter.api.Assertions.assertFalse(defined.isEmpty(), "First variable-defining instruction should define at least one variable");
    }

    @Test
    void testUsedBy() throws Exception {
        // Load a sample class and method (adjust as needed)
        String className = "de/uni_passau/fim/se2/sa/examples/SimpleInteger";
        String methodName = "foo";
        String methodDesc = "()I";

        org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(className);
        org.objectweb.asm.tree.ClassNode cn = new org.objectweb.asm.tree.ClassNode();
        cr.accept(cn, 0);
        org.objectweb.asm.tree.MethodNode mn = null;
        for (Object m : cn.methods) {
            org.objectweb.asm.tree.MethodNode mnode = (org.objectweb.asm.tree.MethodNode) m;
            if (mnode.name.equals(methodName) && mnode.desc.equals(methodDesc)) {
                mn = mnode;
                break;
            }
        }
        assert mn != null;
        // Instruction at index 10
        org.objectweb.asm.tree.AbstractInsnNode insn10 = mn.instructions.get(10);
        System.out.println("Instruction at index 10: opcode=" + insn10.getOpcode() + ", type=" + insn10.getClass().getSimpleName());
        java.util.Collection<br.usp.each.saeg.asm.defuse.Variable> used = DataFlowAnalysis.usedBy(className, mn, insn10);
        System.out.println("Used variables at index 10: " + used);
        org.junit.jupiter.api.Assertions.assertFalse(used.isEmpty(), "Instruction at index 10 should use a variable");
    }
}
