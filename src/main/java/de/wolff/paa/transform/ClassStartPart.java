package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassStartPart implements ClassPart {

  private final byte[] magic = new byte[4];
  private final int minor;
  private final int major;

  ClassStartPart(DataInput source) throws IOException {
    source.readFully(magic);
    minor = source.readUnsignedShort();
    major = source.readUnsignedShort();
  }

  @Override
  public void writeTo(DataOutput sink) throws IOException {
    sink.write(magic);
    sink.writeShort(minor);
    sink.writeShort(major);
  }

}
