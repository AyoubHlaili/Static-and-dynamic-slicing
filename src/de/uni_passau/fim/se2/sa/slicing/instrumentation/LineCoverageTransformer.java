package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class LineCoverageTransformer implements ClassFileTransformer {

  private final String instrumentationTarget;

  public LineCoverageTransformer(String pInstrumentationTarget) {
    instrumentationTarget = pInstrumentationTarget.replace('.', '/');
  }

  @Override
  public byte[] transform(
      ClassLoader pClassLoader,
      String pClassName,
      Class<?> pClassBeingRedefined,
      ProtectionDomain pProtectionDomain,
      byte[] pClassFileBuffer) {
    if (isIgnored(pClassName)) {
      return pClassFileBuffer;
    }

    try {
      org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(pClassFileBuffer);
      org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(cr, org.objectweb.asm.ClassWriter.COMPUTE_FRAMES);
      org.objectweb.asm.ClassVisitor cv = new InstrumentationAdapter(org.objectweb.asm.Opcodes.ASM9, cw);
      cr.accept(cv, 0);
      return cw.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      return pClassFileBuffer; // fallback to original if instrumentation fails
    }
  }

  private boolean isIgnored(String pClassName) {
    return !pClassName.startsWith(instrumentationTarget) || pClassName.endsWith("Test");
  }
}
