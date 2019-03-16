package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ListIterator;

public class AttributesPart extends AbstractClassListPart<AttributesPart.Attribute> {

  AttributesPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected Attribute readPartEntry(DataInput source) throws IOException {
    return new Attribute(source);
  }

  void replaceAttribute(int nameRef, ContentConverter converter) throws IOException {
    ListIterator<Attribute> attributes = entries.listIterator();
    while (attributes.hasNext()) {
      Attribute next = attributes.next();
      if (nameRef == next.nameRef) {
        next = new Attribute(nameRef, converter.apply(next.content));
        attributes.set(next);
      }
    }
  }

  class Attribute implements ClassPart {

    private final int nameRef;
    private final byte[] content;

    private Attribute(DataInput source) throws IOException {
      nameRef = source.readUnsignedShort();
      int contentLength = source.readInt();
      content = new byte[contentLength];
      source.readFully(content);
    }



    private Attribute(int nameRef, byte[] content) {
      super();
      this.nameRef = nameRef;
      this.content = content;
    }



    @Override
    public void writeTo(DataOutput sink) throws IOException {
      sink.writeShort(nameRef);
      sink.writeInt(content.length);
      sink.write(content);
    }

  }

  @FunctionalInterface
  interface ContentConverter {

    byte[] apply(byte[] content) throws IOException;

  }

}
