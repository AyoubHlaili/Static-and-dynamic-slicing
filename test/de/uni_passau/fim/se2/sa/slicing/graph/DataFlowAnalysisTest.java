package de.uni_passau.fim.se2.sa.slicing.graph;

import org.junit.jupiter.api.Test;

public class DataFlowAnalysisTest {
    @Test
    void testDefinedBy() throws Exception {
        // ISTORE defines a local int variable
        org.objectweb.asm.tree.MethodNode mn = new org.objectweb.asm.tree.MethodNode();
        mn.tryCatchBlocks = new java.util.ArrayList<>();
        mn.maxLocals = 5; // Set max locals to accommodate slot 1
        mn.maxStack = 5; // Set max stack size
        mn.desc = "()V"; // Method descriptor for void method with no parameters
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1)); // Push value on stack
        org.objectweb.asm.tree.VarInsnNode store = new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ISTORE, 1);
        mn.instructions.add(store);
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        java.util.Collection<br.usp.each.saeg.asm.defuse.Variable> defs = DataFlowAnalysis.definedBy("TestClass", mn, store);
        java.util.List<String> actual = new java.util.ArrayList<>();
        for (br.usp.each.saeg.asm.defuse.Variable v : defs) actual.add(v.toString());
        java.util.List<String> expected = java.util.Collections.singletonList("L@1");
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    @Test
    void testUsedBy() throws Exception {
        // ILOAD uses a local int variable
        org.objectweb.asm.tree.MethodNode mn = new org.objectweb.asm.tree.MethodNode();
        mn.tryCatchBlocks = new java.util.ArrayList<>();
        mn.maxLocals = 5; // Set max locals to accommodate slot 2
        mn.maxStack = 5; // Set max stack size
        mn.desc = "()V"; // Method descriptor for void method with no parameters
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1)); // Push value on stack
        mn.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ISTORE, 2)); // Store to initialize slot
        org.objectweb.asm.tree.VarInsnNode load = new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ILOAD, 2);
        mn.instructions.add(load);
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP)); // Pop loaded value
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        java.util.Collection<br.usp.each.saeg.asm.defuse.Variable> uses = DataFlowAnalysis.usedBy("TestClass", mn, load);
        java.util.List<String> actual = new java.util.ArrayList<>();
        for (br.usp.each.saeg.asm.defuse.Variable v : uses) actual.add(v.toString());
        java.util.List<String> expected = java.util.Collections.emptyList();
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    @Test
    void testDefinedBy2() throws Exception {
        // IINC defines a local int variable
        org.objectweb.asm.tree.MethodNode mn = new org.objectweb.asm.tree.MethodNode();
        mn.tryCatchBlocks = new java.util.ArrayList<>();
        mn.maxLocals = 5; // Set max locals to accommodate slot 3
        mn.maxStack = 5; // Set max stack size
        mn.desc = "()V"; // Method descriptor for void method with no parameters
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1)); // Push value on stack
        mn.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ISTORE, 3)); // Store to initialize slot
        org.objectweb.asm.tree.IincInsnNode iinc = new org.objectweb.asm.tree.IincInsnNode(3, 1);
        mn.instructions.add(iinc);
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        java.util.Collection<br.usp.each.saeg.asm.defuse.Variable> defs = DataFlowAnalysis.definedBy("TestClass", mn, iinc);
        java.util.List<String> actual = new java.util.ArrayList<>();
        for (br.usp.each.saeg.asm.defuse.Variable v : defs) actual.add(v.toString());
        java.util.List<String> expected = java.util.Collections.singletonList("L@3");
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    @Test
    void testUsedBy2() throws Exception {
        // IINC uses a local int variable
        org.objectweb.asm.tree.MethodNode mn = new org.objectweb.asm.tree.MethodNode();
        mn.tryCatchBlocks = new java.util.ArrayList<>();
        mn.maxLocals = 5; // Set max locals to accommodate slot 4
        mn.maxStack = 5; // Set max stack size
        mn.desc = "()V"; // Method descriptor for void method with no parameters
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1)); // Push value on stack
        mn.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ISTORE, 4)); // Store to initialize slot
        org.objectweb.asm.tree.IincInsnNode iinc = new org.objectweb.asm.tree.IincInsnNode(4, 1);
        mn.instructions.add(iinc);
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        java.util.Collection<br.usp.each.saeg.asm.defuse.Variable> uses = DataFlowAnalysis.usedBy("TestClass", mn, iinc);
        java.util.List<String> actual = new java.util.ArrayList<>();
        for (br.usp.each.saeg.asm.defuse.Variable v : uses) actual.add(v.toString());
        java.util.List<String> expected = java.util.Collections.singletonList("L@4");
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
