package de.wolff.paa.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class ClassConstantsPoolPart extends AbstractClassListPart<ClassConstantPoolEntry> {

  ClassConstantsPoolPart(DataInput source) throws IOException {
    super(source);
  }

  @Override
  protected ClassConstantPoolEntry readPartEntry(DataInput source) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void writePartEntry(DataOutput sink, ClassConstantPoolEntry partEntry) throws IOException {
    // TODO Auto-generated method stub

  }

}
