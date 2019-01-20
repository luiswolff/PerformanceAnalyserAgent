package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassMemberEntry implements ClassPart {

  private final int accessFlags;
  private final int nameRef;
  private final int descriptorRef;
  private final AttributesPart attributes;

  public ClassMemberEntry(DataInput source) throws IOException {
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
