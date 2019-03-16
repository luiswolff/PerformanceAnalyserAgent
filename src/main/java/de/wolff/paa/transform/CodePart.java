package de.wolff.paa.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

class CodePart implements ClassPart {

  private static final int INVOKE_STATIC = 0xB8;

  private int maxStack;
  private int maxLocals;
  private byte[] code;
  private ExceptionTableRaw[] exceptionTable;
  private AttributesPart attributes;

  CodePart(DataInput source) throws IOException {
    maxStack = source.readUnsignedShort();
    maxLocals = source.readUnsignedShort();
    code = new byte[source.readInt()];
    source.readFully(code);
    exceptionTable = new ExceptionTableRaw[source.readUnsignedShort()];
    for (int i = 0; i < exceptionTable.length; i++) {
      exceptionTable[i] = new ExceptionTableRaw(source);
    }
    attributes = new AttributesPart(source);
  }

  void addMethodCallAtStart(int methodRef) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(code.length + 3);
    DataOutput sink = new DataOutputStream(baos);
    sink.writeByte(INVOKE_STATIC);
    sink.writeShort(methodRef);
    sink.write(code);
    code = baos.toByteArray();
    // FIXME: check whether a StackMapTable is present and modify it
  }

  @Override
  public void writeTo(DataOutput sink) throws IOException {
    sink.writeShort(maxStack);
    sink.writeShort(maxLocals);
    sink.writeInt(code.length);
    sink.write(code);
    sink.writeShort(exceptionTable.length);
    for (ExceptionTableRaw exceptionTableRaw : exceptionTable) {
      sink.writeShort(exceptionTableRaw.startPc);
      sink.writeShort(exceptionTableRaw.endPc);
      sink.writeShort(exceptionTableRaw.handlerPc);
      sink.writeShort(exceptionTableRaw.catchType);
    }
    attributes.writeTo(sink);
  }

  private class ExceptionTableRaw {
    private int startPc;
    private int endPc;
    private int handlerPc;
    private int catchType;

    private ExceptionTableRaw(DataInput source) throws IOException {
      startPc = source.readUnsignedShort();
      endPc = source.readUnsignedShort();
      handlerPc = source.readUnsignedShort();
      catchType = source.readUnsignedShort();
    }
  }

}
