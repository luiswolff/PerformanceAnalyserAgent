package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.IOException;

abstract class AttributeEntryReader {

  abstract ClassAttributeEntry read(DataInput source) throws IOException;

  static AttributeEntryReader getInstance(byte tag) {
    // TODO implement
    return null;
  }

}
