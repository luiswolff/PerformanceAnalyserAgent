package de.wolff.paa.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class ClassModifier {

  private final ClassStartPart classStart;
  private final ClassConstantsPoolPart classConstantsPool;
  private final ClassDescriptionPart classDescription;
  private final ClassFieldsPart classFields;
  private final ClassMethodsPart classMethods;
  private final AttributesPart classAttributs;

  ClassModifier(byte[] originalClassfileBuffer) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(originalClassfileBuffer);
    DataInput source = new DataInputStream(bais);

    classStart = new ClassStartPart(source);
    classConstantsPool = new ClassConstantsPoolPart(source);
    classDescription = new ClassDescriptionPart(source);
    classFields = new ClassFieldsPart(source);
    classMethods = new ClassMethodsPart(source);
    classAttributs = new AttributesPart(source);

  }

  int addMethodReference(String name, String invocationMethod) {

    name = toByteCodeClassName(name);
    // @formatter:off
    return classConstantsPool.newMethodRef()
        .classRef(name)
        // At the moment we only support parameter less, static and void methods
        .nameAndType(invocationMethod, "()V")
        .addToPool();
    // @formatter:on
  }

  private String toByteCodeClassName(String name) {
    return "L" + name.replace('.', '/');
  }

  void addInvokeStaticToBegin(int constantPoolIndex, String redefineMethodName,
      Class<?>[] redefineMethodParameterTypes, Integer redefineMethodSignature) {
    // TODO Auto-generated method stub
    System.out.println("Invoke method from constant pool " + constantPoolIndex);
  }

  byte[] toByteCode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    writePartsTo(baos);
    return baos.toByteArray();
  }

  private void writePartsTo(ByteArrayOutputStream baos) throws IOException {
    DataOutput sink = new DataOutputStream(baos);

    for (ClassPart part : allParts()) {
      part.writeTo(sink);
    }
  }

  private Iterable<ClassPart> allParts() {
    // @formatter:off
    return Arrays.asList( 
        classStart,
        classConstantsPool,
        classDescription,
        classFields,
        classMethods,
        classAttributs
    );
    // @formatter:on
  }

}
