package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassMethodsPart extends AbstractClassListPart<ClassMethodEntry> {

  ClassMethodsPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected ClassMethodEntry readPartEntry(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void writePartEntry(DataOutput sink, ClassMethodEntry partEntry) throws IOException {
    // TODO Auto-generated method stub

  }

}
