package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker;

public class LineCoverageTransformerTest {
    
    private LineCoverageTransformer transformer;
    
    @BeforeEach
    void setUp() {
        // Reset coverage tracker before each test
        CoverageTracker.reset();
        transformer = new LineCoverageTransformer("de.uni_passau.fim.se2.sa.examples");
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        CoverageTracker.reset();
    }
    
    @Test
    void testTransform() throws Exception {
        // Get original bytecode for SimpleInteger class
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleInteger";
        ClassReader classReader = new ClassReader(className);
        byte[] originalBytecode = classReader.b;
        
        // Transform the bytecode
        byte[] transformedBytecode = transformer.transform(
            getClass().getClassLoader(),
            className.replace('.', '/'),
            null,
            null,
            originalBytecode
        );
        
        // Verify transformation occurred (bytecode should be different)
        org.junit.jupiter.api.Assertions.assertNotNull(transformedBytecode, 
            "Transformed bytecode should not be null");
        org.junit.jupiter.api.Assertions.assertTrue(transformedBytecode.length > 0, 
            "Transformed bytecode should not be empty");
        
        // The transformed bytecode should be different from original (contains instrumentation)
        org.junit.jupiter.api.Assertions.assertFalse(
            java.util.Arrays.equals(originalBytecode, transformedBytecode),
            "Transformed bytecode should be different from original"
        );
    }
    
    @Test
    void testTransformIgnoresTestClasses() throws Exception {
        // Test that test classes are ignored during transformation
        String testClassName = "de.uni_passau.fim.se2.sa.examples.SimpleIntegerTest";
        ClassReader classReader = new ClassReader(testClassName);
        byte[] originalBytecode = classReader.b;
        
        // Transform the bytecode
        byte[] transformedBytecode = transformer.transform(
            getClass().getClassLoader(),
            testClassName.replace('.', '/'),
            null,
            null,
            originalBytecode
        );
        
        // Test classes should not be transformed (return original bytecode)
        org.junit.jupiter.api.Assertions.assertArrayEquals(originalBytecode, transformedBytecode,
            "Test classes should not be transformed");
    }
    
    @Test
    void testTransformIgnoresNonTargetPackages() throws Exception {
        // Test that classes outside target package are ignored
        String nonTargetClassName = "java.lang.String";
        
        // Create a simple bytecode for testing
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "java/lang/String", null, "java/lang/Object", null);
        cw.visitEnd();
        byte[] originalBytecode = cw.toByteArray();
        
        // Transform the bytecode
        byte[] transformedBytecode = transformer.transform(
            getClass().getClassLoader(),
            "java/lang/String",
            null,
            null,
            originalBytecode
        );
        
        // Non-target classes should not be transformed (return original bytecode)
        org.junit.jupiter.api.Assertions.assertArrayEquals(originalBytecode, transformedBytecode,
            "Non-target package classes should not be transformed");
    }
    
    @Test
    void testTransformWithTargetClass() throws Exception {
        // Test transformation of a class in the target package
        String targetClassName = "de.uni_passau.fim.se2.sa.examples.Calculator";
        ClassReader classReader = new ClassReader(targetClassName);
        byte[] originalBytecode = classReader.b;
        
        // Transform the bytecode
        byte[] transformedBytecode = transformer.transform(
            getClass().getClassLoader(),
            targetClassName.replace('.', '/'),
            null,
            null,
            originalBytecode
        );
        
        // Target classes should be transformed
        org.junit.jupiter.api.Assertions.assertNotNull(transformedBytecode,
            "Transformed bytecode should not be null");
        org.junit.jupiter.api.Assertions.assertFalse(
            java.util.Arrays.equals(originalBytecode, transformedBytecode),
            "Target package classes should be transformed"
        );
    }
    
    @Test
    void testTransformWithInvalidBytecode() {
        // Test transformation with invalid bytecode
        byte[] invalidBytecode = "invalid bytecode".getBytes();
        
        // Transform should handle invalid bytecode gracefully
        byte[] result = transformer.transform(
            getClass().getClassLoader(),
            "de/uni_passau/fim/se2/sa/examples/TestClass",
            null,
            null,
            invalidBytecode
        );
        
        // Should return original bytecode when transformation fails
        org.junit.jupiter.api.Assertions.assertArrayEquals(invalidBytecode, result,
            "Invalid bytecode should be returned unchanged when transformation fails");
    }
    
    @Test
    void testTransformWithNullBytecode() {
        // Test transformation with null bytecode
        byte[] result = transformer.transform(
            getClass().getClassLoader(),
            "de/uni_passau/fim/se2/sa/examples/TestClass",
            null,
            null,
            null
        );
        
        // Should return null when input is null
        org.junit.jupiter.api.Assertions.assertNull(result,
            "Null bytecode should result in null output");
    }
    
    @Test
    void testTransformWithEmptyBytecode() {
        // Test transformation with empty bytecode
        byte[] emptyBytecode = new byte[0];
        
        byte[] result = transformer.transform(
            getClass().getClassLoader(),
            "de/uni_passau/fim/se2/sa/examples/TestClass",
            null,
            null,
            emptyBytecode
        );
        
        // Should return original empty bytecode when transformation fails
        org.junit.jupiter.api.Assertions.assertArrayEquals(emptyBytecode, result,
            "Empty bytecode should be returned unchanged when transformation fails");
    }
    
    @Test
    void testConstructorWithDifferentTargets() {
        // Test constructor with different instrumentation targets
        LineCoverageTransformer transformer1 = new LineCoverageTransformer("com.example");
        LineCoverageTransformer transformer2 = new LineCoverageTransformer("de.uni_passau.fim.se2.sa");
        
        org.junit.jupiter.api.Assertions.assertNotNull(transformer1,
            "Transformer should be created with any valid target");
        org.junit.jupiter.api.Assertions.assertNotNull(transformer2,
            "Transformer should be created with any valid target");
    }
    
    @Test
    void testTransformWithDifferentClassLoaders() throws Exception {
        // Test transformation with different class loaders
        String className = "de.uni_passau.fim.se2.sa.examples.SimpleInteger";
        ClassReader classReader = new ClassReader(className);
        byte[] originalBytecode = classReader.b;
        
        // Test with null class loader
        byte[] result1 = transformer.transform(
            null,
            className.replace('.', '/'),
            null,
            null,
            originalBytecode
        );
        
        // Test with current class loader
        byte[] result2 = transformer.transform(
            getClass().getClassLoader(),
            className.replace('.', '/'),
            null,
            null,
            originalBytecode
        );
        
        org.junit.jupiter.api.Assertions.assertNotNull(result1,
            "Transformation should work with null class loader");
        org.junit.jupiter.api.Assertions.assertNotNull(result2,
            "Transformation should work with valid class loader");
        
        // Both results should be transformed (not equal to original)
        org.junit.jupiter.api.Assertions.assertFalse(
            java.util.Arrays.equals(originalBytecode, result1),
            "Result with null classloader should be transformed"
        );
        org.junit.jupiter.api.Assertions.assertFalse(
            java.util.Arrays.equals(originalBytecode, result2),
            "Result with valid classloader should be transformed"
        );
    }
}
