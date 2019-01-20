package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class AttributeEntry implements ClassPart {

  private final int nameRef;
  private final byte[] content;

  AttributeEntry(DataInput source) throws IOException {
    nameRef = source.readUnsignedShort();
    int contentLength = source.readInt();
    content = new byte[contentLength];
    source.readFully(content);
  }

  @Override
  public void writeTo(DataOutput sink) throws IOException {
    sink.writeShort(nameRef);
    sink.writeInt(content.length);
    sink.write(content);
  }

}
