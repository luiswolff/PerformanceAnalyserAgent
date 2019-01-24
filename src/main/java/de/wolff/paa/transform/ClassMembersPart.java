package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassMembersPart extends AbstractClassListPart<ClassMembersPart.Member> {

  ClassMembersPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected Member readPartEntry(DataInput source) throws IOException {
    return new Member(source);
  }

  AttributesPart findMethod(int accessFlags, int nameRef, int descriptorRef) {
    for (Member entry : entries) {
      if (entry.accessFlags == accessFlags && entry.nameRef == nameRef
          && entry.descriptorRef == descriptorRef) {
        return entry.attributes;
      }
    }
    return null;
  }

  class Member implements ClassPart {

    private final int accessFlags;
    private final int nameRef;
    private final int descriptorRef;
    private final AttributesPart attributes;

    public Member(DataInput source) throws IOException {
      accessFlags = source.readUnsignedShort();
      nameRef = source.readUnsignedShort();
      descriptorRef = source.readUnsignedShort();
      attributes = new AttributesPart(source);
    }

    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeShort(accessFlags);
      sink.writeShort(nameRef);
      sink.writeShort(descriptorRef);
      attributes.writeTo(sink);
    }

  }

}
