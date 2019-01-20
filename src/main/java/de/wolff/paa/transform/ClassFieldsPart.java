package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassFieldsPart extends AbstractClassListPart<ClassFieldEntry> {

  ClassFieldsPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected ClassFieldEntry readPartEntry(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void writePartEntry(DataOutput sink, ClassFieldEntry partEntry) throws IOException {
    // TODO Auto-generated method stub

  }

}
