package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassDescriptionPart implements ClassPart {

  private final int accessFlags;
  private final int thisClassNameRef;
  private final int superClassNameRef;
  private final int[] interfaceClassNameRefs;

  public ClassDescriptionPart(DataInput source) throws IOException {
    accessFlags = source.readUnsignedShort();
    thisClassNameRef = source.readUnsignedShort();
    superClassNameRef = source.readUnsignedShort();

    int interfaceCount = source.readUnsignedShort();
    interfaceClassNameRefs = new int[interfaceCount];
    for (int i = 0; i < interfaceCount; i++) {
      interfaceClassNameRefs[i] = source.readUnsignedShort();
    }
  }

  @Override
  public void writeTo(DataOutput sink) throws IOException {
    sink.writeShort(accessFlags);
    sink.writeShort(thisClassNameRef);
    sink.writeShort(superClassNameRef);
    sink.writeShort(interfaceClassNameRefs.length);
    for (int interfaceClassNameRef : interfaceClassNameRefs) {
      sink.writeShort(interfaceClassNameRef);
    }
  }

}
