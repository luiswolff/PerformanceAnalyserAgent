package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.IOException;

public class AttributesPart extends AbstractClassListPart<AttributeEntry> {

  AttributesPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected AttributeEntry readPartEntry(DataInput source) throws IOException {
    return new AttributeEntry(source);
  }

}
