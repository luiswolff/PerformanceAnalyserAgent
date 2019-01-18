package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.IOException;

abstract class ClasspoolEntryReader {

  abstract ClasspoolEntry read(DataInput source) throws IOException;

  static ClasspoolEntryReader getInstance(byte tag) {
    // TODO implement
    return null;
  }

}
