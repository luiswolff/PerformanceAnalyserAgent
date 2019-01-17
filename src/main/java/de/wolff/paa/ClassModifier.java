package de.wolff.paa;

class ClassModifier {

  private final byte[] originalClassfileBuffer;

  ClassModifier(byte[] originalClassfileBuffer) {
    this.originalClassfileBuffer = originalClassfileBuffer;
    // TODO current class file definition
  }

  short addMethodReference(String name, String invocationMethod) {
    // TODO Auto-generated method stub
    return 0;
  }

  void addInvokeStaticToBegin(int constantPoolIndex, String redefineMethodName,
      Class<?>[] redefineMethodParameterTypes, Integer redefineMethodSignature) {
    // TODO Auto-generated method stub

  }

  byte[] toByteCode() {
    // TODO read generated class file definition
    return originalClassfileBuffer;
  }

}
