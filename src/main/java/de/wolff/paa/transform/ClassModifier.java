package de.wolff.paa.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class ClassModifier {

  private final List<ClassPart> classParts;

  ClassModifier(byte[] originalClassfileBuffer) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(originalClassfileBuffer);
    DataInput source = new DataInputStream(bais);

    // @formatter:off
    classParts = Arrays.asList( 
        new ClassStartPart(source),
        new ClassConstantsPoolPart(source),
        new ClassDescriptionPart(source),
        new ClassFieldsPart(source),
        new ClassMethodsPart(source),
        new AttributesPart(source)
    );
    // @formatter:on
  }

  int addMethodReference(String name, String invocationMethod) {
    // TODO Auto-generated method stub
    return 0;
  }

  void addInvokeStaticToBegin(int constantPoolIndex, String redefineMethodName,
      Class<?>[] redefineMethodParameterTypes, Integer redefineMethodSignature) {
    // TODO Auto-generated method stub

  }

  byte[] toByteCode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutput sink = new DataOutputStream(baos);

    for (ClassPart part : classParts) {
      part.writeTo(sink);
    }

    return baos.toByteArray();
  }

}
