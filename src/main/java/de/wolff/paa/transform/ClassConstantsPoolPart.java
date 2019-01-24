package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

class ClassConstantsPoolPart
    extends AbstractClassListPart<ClassConstantsPoolPart.Entry> {

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
    // @formatter:off
    entries.stream()
        .filter(RefEntry.class::isInstance)
        .map(RefEntry.class::cast)
        .forEach(RefEntry::loadReferences);
    // @formatter:on
  }

  @Override
  protected Entry readPartEntry(DataInput source) throws IOException {
    if (entries.size() == 0) {
      return new EmptyEntry();
    }
    byte tag = source.readByte();
    return toClassConstantPoolEntry(tag, source);
  }

  private Entry toClassConstantPoolEntry(byte tag, DataInput source)
      throws IOException {
    switch (tag) {
      case CONSTANT_UTF8:
        int length = source.readUnsignedShort();
        byte[] rawContent = new byte[length];
        source.readFully(rawContent);
        return new Utf8Entry(rawContent);
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
        return new MemberRefEntry(tag, source.readUnsignedShort(), source.readUnsignedShort());
      case CONSTANT_INVOKE_DYNAMIC:
        return new InvokeDynamicEntry(source.readUnsignedShort(), source.readUnsignedShort());
      case CONSTANT_NAME_AND_TYPE:
        return new NameAndTypeRefEntry(source.readUnsignedShort(), source.readUnsignedShort());
      default:
        throw new IOException("Unknown constant pool tag " + tag);
    }
  }

  MethodRefBuilder newMethodRef() {
    return new MethodRefBuilder();
  }

  class MethodRefBuilder {

    private MemberRefEntry methodRef;
    private StringRefEntry classRef;
    private NameAndTypeRefEntry nameAndType;

    MethodRefBuilder classRef(String className) {
      classRef = new StringRefEntry(CONSTANT_CLASS);
      classRef.utf8Value = new Utf8Entry(className);
      return this;
    }

    MethodRefBuilder nameAndType(String name, String type) {
      nameAndType = new NameAndTypeRefEntry();
      nameAndType.name = new Utf8Entry(name);
      nameAndType.type = new Utf8Entry(type);
      return this;
    }

    int addToPool() {
      createMethodRef();
      addNewEntries();

      return entries.indexOf(methodRef);
    }

    private void createMethodRef() {
      methodRef = new MemberRefEntry(CONSTANT_METHOD_REF);
      methodRef.member = classRef;
      methodRef.nameAndType = nameAndType;
    }

    private void addNewEntries() {
      Collection<Entry> newEntries = Arrays.asList(methodRef, classRef, classRef.utf8Value,
          nameAndType, nameAndType.name, nameAndType.type);
      entries.addAll(newEntries);
    }

  }

  abstract class Entry implements ClassPart {

    int getIndex() {
      return entries.indexOf(this);
    }

  }

  private abstract class RefEntry extends Entry {

    abstract void loadReferences();

  }

  class StringRefEntry extends RefEntry {

    private final byte tag;
    private final int ref;
    private Utf8Entry utf8Value;

    StringRefEntry(byte tag) {
      this(tag, -1);
    }

    private StringRefEntry(byte tag, int ref) {
      this.tag = tag;
      this.ref = ref;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(tag);
      sink.writeShort(utf8Value.getIndex());
    }

    @Override
    void loadReferences() {
      utf8Value = getEntryWithIndex(ref, Utf8Entry.class);
    }

  }

  class MemberRefEntry extends RefEntry {

    private final byte tag;
    private final int memberRef;
    private final int nameAndTypeRef;
    private StringRefEntry member;
    private NameAndTypeRefEntry nameAndType;

    private MemberRefEntry(byte tag) {
      this(tag, -1, -1);
    }

    private MemberRefEntry(byte tag, int memberRef, int nameAndTypeRef) {
      super();
      this.tag = tag;
      this.memberRef = memberRef;
      this.nameAndTypeRef = nameAndTypeRef;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(tag);
      sink.writeShort(member.getIndex());
      sink.writeShort(nameAndType.getIndex());
    }

    @Override
    void loadReferences() {
      member = getEntryWithIndex(memberRef, StringRefEntry.class);
      nameAndType = getEntryWithIndex(nameAndTypeRef, NameAndTypeRefEntry.class);
    }

  }

  class NameAndTypeRefEntry extends RefEntry {

    private final int nameRef;
    private final int typeRef;
    private Utf8Entry name;
    private Utf8Entry type;

    private NameAndTypeRefEntry() {
      this(-1, -1);
    }

    private NameAndTypeRefEntry(int nameRef, int typeRef) {
      super();
      this.nameRef = nameRef;
      this.typeRef = typeRef;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(CONSTANT_NAME_AND_TYPE);
      sink.writeShort(name.getIndex());
      sink.writeShort(type.getIndex());
    }

    @Override
    void loadReferences() {
      name = getEntryWithIndex(nameRef, Utf8Entry.class);
      type = getEntryWithIndex(typeRef, Utf8Entry.class);
    }

  }

  class NumberEntry extends Entry {

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

  class HighNumberEntry extends Entry {

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

  class Utf8Entry extends Entry {

    private final String content;

    private Utf8Entry(byte[] content) {
      this.content = new String(content, StandardCharsets.UTF_8);
    }

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

  class InvokeDynamicEntry extends RefEntry {

    private final int bootstrapMethodAttrIndex;
    private final int nameAndTypeRef;
    private NameAndTypeRefEntry nameAndType;

    InvokeDynamicEntry(int bootstrapMethodAttrIndex, int nameAndTypeRef) {
      this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
      this.nameAndTypeRef = nameAndTypeRef;
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeByte(CONSTANT_INVOKE_DYNAMIC);
      sink.writeByte(bootstrapMethodAttrIndex);
      sink.writeByte(nameAndType.getIndex());
    }

    @Override
    void loadReferences() {
      nameAndType = getEntryWithIndex(nameAndTypeRef, NameAndTypeRefEntry.class);
    }

  }

  private class EmptyEntry extends Entry {

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      // this class writes nothing
    }

  }

  <T extends Entry> T getEntryWithIndex(int index, Class<T> expectedType) {
    Entry entry = entries.get(index);
    if (expectedType.isInstance(entry)) {
      return expectedType.cast(entry);
    }
    throw new ConstantPoolException(index, expectedType.getSimpleName());
  }

  int findIndex(String content) {
    for (int i = 1; i < entries.size(); i++) {
      Entry entry = entries.get(i);
      if (entry instanceof Utf8Entry && content.equals(((Utf8Entry) entry).content)) {
        return i;
      }
    }
    throw new ConstantPoolException(content);
  }

}
