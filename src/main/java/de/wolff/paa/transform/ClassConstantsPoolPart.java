package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class ClassConstantsPoolPart
    extends AbstractClassListPart<ClassConstantPoolEntry> {

  private static final byte CONSTANT_UTF8 = 1;
  private static final byte CONSTANT_INTEGER = 3;
  private static final byte CONSTANT_FLOAT = 4;
  private static final byte CONSTANT_LONG = 5; // TODO: needs tow constant pool indices
  private static final byte CONSTANT_DOUBLE = 6; // TODO: needs tow constant pool indices
  private static final byte CONSTANT_CLASS = 7;
  private static final byte CONSTANT_STRING = 8;
  private static final byte CONSTANT_FIELD_REF = 9;
  private static final byte CONSTANT_METHOD_REF = 10;
  private static final byte CONSTANT_INTERFACE_REF = 11;
  private static final byte CONSTANT_NAME_AND_TYPE = 12;
  // as fare as I know, Java sources don't produce such byte code
  // private static final byte CONSTANT_METHOD_HANDLE = 15;
  private static final byte CONSTANT_METHOD_TYPE = 16;
  private static final byte CONSTANT_INVOKE_DYNAMIC = 18;


  ClassConstantsPoolPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected ClassConstantPoolEntry readPartEntry(DataInput source) throws IOException {
    if (partEntries.size() == 0) {
      return new EmptyEntry();
    }
    byte tag = source.readByte();
    return toClassConstantPoolEntry(tag, source);
  }

  private ClassConstantPoolEntry toClassConstantPoolEntry(byte tag, DataInput source)
      throws IOException {
    switch (tag) {
      case CONSTANT_UTF8:
        int length = source.readUnsignedShort();
        byte[] rawContent = new byte[length];
        source.readFully(rawContent);
        String content = new String(rawContent, StandardCharsets.UTF_8);
        return new Utf8Entry(content);
      case CONSTANT_INTEGER:
      case CONSTANT_FLOAT:
        return new NumberEntry(tag, source.readInt());
      case CONSTANT_LONG:
      case CONSTANT_DOUBLE:
        return new HighNumberEntry(tag, source.readInt(), source.readInt());
      case CONSTANT_CLASS:
      case CONSTANT_STRING:
      case CONSTANT_METHOD_TYPE:
        return new StringRefEntry(tag, source.readUnsignedShort());
      case CONSTANT_FIELD_REF:
      case CONSTANT_METHOD_REF:
      case CONSTANT_INTERFACE_REF:
      case CONSTANT_NAME_AND_TYPE:
      case CONSTANT_INVOKE_DYNAMIC:
        return new MemberRefEntry(tag, source.readUnsignedShort(), source.readUnsignedShort());
      default:
        throw new IOException("Unknown constant pool tag " + tag);
    }
  }

  private class StringRefEntry implements ClassConstantPoolEntry {

    private final byte tag;
    private final int ref;

    private StringRefEntry(byte tag, int ref) throws IOException {
      this.tag = tag;
      this.ref = ref;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(tag);
      sink.writeShort(ref);
    }

  }

  private class MemberRefEntry implements ClassConstantPoolEntry {

    private final byte tag;
    private final int classRef;
    private final int nameAndTypeRef;

    public MemberRefEntry(byte tag, int classRef, int nameAndTypeRef) {
      super();
      this.tag = tag;
      this.classRef = classRef;
      this.nameAndTypeRef = nameAndTypeRef;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(tag);
      sink.writeShort(classRef);
      sink.writeShort(nameAndTypeRef);
    }

  }

  private class NumberEntry implements ClassConstantPoolEntry {

    private final byte tag;
    private final int bytes;

    public NumberEntry(byte tag, int bytes) {
      super();
      this.tag = tag;
      this.bytes = bytes;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(tag);
      sink.writeInt(bytes);
    }

  }

  private class HighNumberEntry implements ClassConstantPoolEntry {

    private final byte tag;
    private final int highBytes;
    private final int lowBytes;

    public HighNumberEntry(byte tag, int highBytes, int lowBytes) {
      super();
      this.tag = tag;
      this.highBytes = highBytes;
      this.lowBytes = lowBytes;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(tag);
      sink.writeInt(highBytes);
      sink.writeInt(lowBytes);
    }

  }

  private class Utf8Entry implements ClassConstantPoolEntry {

    private final String content;

    private Utf8Entry(String content) {
      this.content = content;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      byte[] content = this.content.getBytes(StandardCharsets.UTF_8);
      sink.write(CONSTANT_UTF8);
      sink.writeShort(content.length);
      sink.write(content);
    }

  }

  private class EmptyEntry implements ClassConstantPoolEntry {

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      // this class writes nothing
    }

  }

}
