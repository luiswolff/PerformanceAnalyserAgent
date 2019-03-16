package de.wolff.paa.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

class ClassModifier {

  private final ClassStartPart classStart;
  private final ClassConstantsPoolPart classConstantsPool;
  private final ClassDescriptionPart classDescription;
  private final ClassMembersPart classFields;
  private final ClassMembersPart classMethods;
  private final AttributesPart classAttributs;

  ClassModifier(byte[] originalClassfileBuffer) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(originalClassfileBuffer);
    DataInput source = new DataInputStream(bais);

    classStart = new ClassStartPart(source);
    classConstantsPool = new ClassConstantsPoolPart(source);
    classDescription = new ClassDescriptionPart(source);
    classFields = new ClassMembersPart(source);
    classMethods = new ClassMembersPart(source);
    classAttributs = new AttributesPart(source);

  }

  int addMethodRef(Class<?> clazz, String invocationMethod) {

    String name = toClassRef(clazz);
    // @formatter:off
    return classConstantsPool.newMethodRef()
        .classRef(name)
        // At the moment we only support parameter less, static and void methods
        .nameAndType(invocationMethod, "()V")
        .addToPool();
    // @formatter:on
  }

  void pushInvokeStatic(int methodRef, String targetName,
      Class<?>[] targetParams, int accessflags) throws IOException {
    AttributesPart method = classMethods.findMethod(accessflags, //
        classConstantsPool.findIndex(targetName),
        classConstantsPool.findIndex("(" + toClassRef(targetParams) + ")V"));

    CodeModifier modifier = new CodeModifier(methodRef);
    method.replaceAttribute(classConstantsPool.findIndex("Code"), modifier::modify);

  }

  private String toClassRef(Class<?>[] targetParams) {
    return Arrays.stream(targetParams).map(this::toClassRef).collect(Collectors.joining(","));
  }

  private String toClassRef(Class<?> clazz) {
    return clazz.getName().replace('.', '/');
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

  private class CodeModifier {

    private final int methodRef;

    private CodeModifier(int methodRef) {
      this.methodRef = methodRef;
    }

    private byte[] modify(byte[] methodCode) throws IOException {
      CodePart codePart = new CodePart(new DataInputStream(new ByteArrayInputStream(methodCode)));
      codePart.addMethodCallAtStart(methodRef);
      ByteArrayOutputStream baos = new ByteArrayOutputStream(methodCode.length);
      codePart.writeTo(new DataOutputStream(baos));
      return baos.toByteArray();
    }

  }

}
