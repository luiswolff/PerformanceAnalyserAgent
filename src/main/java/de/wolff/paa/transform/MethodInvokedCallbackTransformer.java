package de.wolff.paa.transform;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MethodInvokedCallbackTransformer implements ClassFileTransformer {

  private static final Logger LOGGER =
      Logger.getLogger(MethodInvokedCallbackTransformer.class.getName());

  private Class<?> invocationClass;
  private String invocationMethod;
  private String redefineMethodName;
  private Class<?>[] redefineMethodParameterTypes;
  private Integer redefineMethodSignature;

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
      throws IllegalClassFormatException {

    LOGGER.finer("Transformation method invoked for class " + className);

    if (canRedefine() && containsRedefineMethod(classBeingRedefined.getMethods())) {

      LOGGER.fine("Redefine class definition of " + className);

      try {
        ClassModifier classModifier = new ClassModifier(classfileBuffer);
        int constantPoolIndex =
            classModifier.addMethodReference(invocationClass.getName(), this.invocationMethod);
        classModifier.addInvokeStaticToBegin(constantPoolIndex, redefineMethodName,
            redefineMethodParameterTypes, redefineMethodSignature);
        return classModifier.toByteCode();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "error transforming class " + className, e);
      }
    }
    return classfileBuffer;
  }

  private boolean canRedefine() {
    return !Objects.isNull(invocationClass) //
        && !Objects.isNull(invocationMethod) //
        && !Objects.isNull(redefineMethodName) //
        && !Objects.isNull(redefineMethodParameterTypes) //
        && !Objects.isNull(redefineMethodSignature);
  }

  private boolean containsRedefineMethod(Method[] methods) {
    return Arrays.stream(methods).anyMatch(this::matchMethodDefinition);
  }

  private boolean matchMethodDefinition(Method method) {
    return Optional.of(method) //
        .filter(this::matchName) //
        .filter(this::matchParameterTypes) //
        .filter(this::matchModifiers) //
        .isPresent();
  }

  private boolean matchName(Method method) {
    return Objects.equals(redefineMethodName, method.getName());
  }

  private boolean matchParameterTypes(Method method) {
    return Arrays.deepEquals(redefineMethodParameterTypes, method.getParameterTypes());
  }

  private boolean matchModifiers(Method method) {
    return Objects.equals(redefineMethodSignature, method.getModifiers());
  }

  public void setInvocationClass(Class<?> invocationClass) {
    this.invocationClass = Objects.requireNonNull(invocationClass);
  }

  public void setInvocationMethod(String invocationMethod) {
    this.invocationMethod = Objects.requireNonNull(invocationMethod);
  }

  public void setRedefineMethodName(String redefineMethodName) {
    this.redefineMethodName = Objects.requireNonNull(redefineMethodName);
  }

  public void setRedefineMathodParameterTypes(Class<?>[] redefineMethodParameterTypes) {
    this.redefineMethodParameterTypes = Objects.requireNonNull(redefineMethodParameterTypes);
  }

  public void setRedefineMethodModifiers(Integer redefineMethodSignature) {
    this.redefineMethodSignature = Objects.requireNonNull(redefineMethodSignature);
  }

}
