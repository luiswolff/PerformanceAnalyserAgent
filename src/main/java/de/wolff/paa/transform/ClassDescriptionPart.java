package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassDescriptionPart implements ClassPart {

  private final byte accessFlags;
  private final byte thisClassNameRef;
  private final byte superClassNameRef;
  private final byte[] interfaceClassNameRefs;

  public ClassDescriptionPart(DataInput source) throws IOException {
    accessFlags = source.readByte();
    thisClassNameRef = source.readByte();
    superClassNameRef = source.readByte();

    int interfaceCount = source.readUnsignedByte();
    interfaceClassNameRefs = new byte[interfaceCount];
    source.readFully(interfaceClassNameRefs);
  }

  @Override
  public void writeTo(DataOutput sink) throws IOException {
    sink.writeByte(accessFlags);
    sink.writeByte(thisClassNameRef);
    sink.writeByte(superClassNameRef);
    sink.writeByte(interfaceClassNameRefs.length);
    sink.write(interfaceClassNameRefs);
  }

}
