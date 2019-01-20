package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ClassAttributesPart extends AbstractClassListPart<ClassAttributeEntry> {

  ClassAttributesPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected ClassAttributeEntry readPartEntry(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void writePartEntry(DataOutput sink, ClassAttributeEntry partEntry) throws IOException {
    // TODO Auto-generated method stub

  }

}
