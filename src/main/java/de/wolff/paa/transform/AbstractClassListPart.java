package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

abstract class AbstractClassListPart<T extends ClassPart> implements ClassPart {

  protected final ArrayList<T> partEntries;

  AbstractClassListPart(DataInput source) throws IOException {
    int count = source.readUnsignedShort();
    partEntries = new ArrayList<T>(count);
    for (int i = 0; i < count; i++) {
      T partEntry = readPartEntry(source);
      partEntries.add(partEntry);
    }
  }

  protected abstract T readPartEntry(DataInput source) throws IOException;

  @Override
  public void writeTo(DataOutput sink) throws IOException {
    sink.writeShort(partEntries.size());
    for (T partEntry : partEntries) {
      partEntry.writeTo(sink);
    }
  }

}
