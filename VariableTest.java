import br.usp.each.saeg.asm.defuse.Variable;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class VariableTest {
    public static void main(String[] args) {
        try {
            Class<?> varClass = Variable.class;
            System.out.println("Variable class: " + varClass);
            System.out.println("Is interface: " + varClass.isInterface());
            System.out.println("Is abstract: " + java.lang.reflect.Modifier.isAbstract(varClass.getModifiers()));
            System.out.println("Is enum: " + varClass.isEnum());
            
            System.out.println("\nConstructors:");
            for (Constructor<?> c : varClass.getDeclaredConstructors()) {
                System.out.println("  " + c);
            }
            
            System.out.println("\nMethods:");
            for (Method m : varClass.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && 
                    m.getReturnType() == varClass) {
                    System.out.println("  Static factory: " + m);
                }
            }
            
            System.out.println("\nSuper class: " + varClass.getSuperclass());
            System.out.println("Interfaces: " + java.util.Arrays.toString(varClass.getInterfaces()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
