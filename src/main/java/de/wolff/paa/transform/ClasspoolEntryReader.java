package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.IOException;

abstract class ClasspoolEntryReader {

  abstract ClassConstantPoolEntry read(DataInput source) throws IOException;

  static ClasspoolEntryReader getInstance(byte tag) {
    // TODO implement
    return null;
  }

}
