package de.wolff.paa;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

class PerformanceAnalyserClassFileTransformer implements ClassFileTransformer {

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
      throws IllegalClassFormatException {
    // TODO class file buffer where not modified yet
    return classfileBuffer;
  }

  public void setInvocationClass(Class<?> targetClass) {
    // TODO Auto-generated method stub

  }

  public void setInvocationField(String targetField) {
    // TODO Auto-generated method stub

  }

  public void setInvocationMethod(String targetMethod) {
    // TODO Auto-generated method stub

  }

  public void setRedefineMethodName(String redefineMethodName) {
    // TODO Auto-generated method stub

  }

  public void setRedefineMathodParameterTypes(Class<?>[] redefineMethodParameterTypes) {
    // TODO Auto-generated method stub

  }

}