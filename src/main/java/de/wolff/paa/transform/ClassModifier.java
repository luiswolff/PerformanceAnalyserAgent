package de.wolff.paa.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ClassModifier {

  private final byte[] magic = new byte[4];
  private final int minor;
  private final int major;
  private final List<ClasspoolEntry> classpoolEntries;
  private final ClassDescription classDescription;
  private final List<FieldDescription> fieldDescriptions;
  private final List<MethodDescription> methodDescriptions;
  private final List<AttributeEntry> attributeEntries;

  ClassModifier(byte[] originalClassfileBuffer) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(originalClassfileBuffer);
    DataInput source = new DataInputStream(bais);

    source.readFully(magic);
    minor = source.readUnsignedShort();
    major = source.readUnsignedShort();

    classpoolEntries = readAsList(source, this::readClasspoolEntries);
    classDescription = new ClassDescription(source);
    fieldDescriptions = readAsList(source, this::readFieldDescriptions);
    methodDescriptions = readAsList(source, this::readMethodDescriptions);
    attributeEntries = readAsList(source, this::readAttributeEntries);
  }

  private <T extends ClassPart> List<T> readAsList(DataInput source,
      DataInputMapper<T> mapper) throws IOException {
    int count = source.readUnsignedShort();
    ArrayList<T> classParts = new ArrayList<T>(count);
    for (int i = 0; i < count; i++) {
      T classpart = mapper.read(source);
      classParts.add(classpart);
    }
    return classParts;
  }

  private ClasspoolEntry readClasspoolEntries(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private FieldDescription readFieldDescriptions(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private MethodDescription readMethodDescriptions(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private AttributeEntry readAttributeEntries(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
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

    sink.write(magic);
    sink.writeShort(minor);
    sink.writeShort(major);

    sink.writeShort(classpoolEntries.size());
    writeClassParts(sink, classpoolEntries);

    writeClassParts(sink, Collections.singletonList(classDescription));

    sink.writeShort(fieldDescriptions.size());
    writeClassParts(sink, fieldDescriptions);

    sink.writeShort(methodDescriptions.size());
    writeClassParts(sink, methodDescriptions);

    sink.writeShort(attributeEntries.size());
    writeClassParts(sink, attributeEntries);

    return baos.toByteArray();
  }

  private void writeClassParts(DataOutput sink, List<? extends ClassPart> classParts)
      throws IOException {

  }

  private interface DataInputMapper<T extends ClassPart> {

    T read(DataInput source) throws IOException;

  }

}
