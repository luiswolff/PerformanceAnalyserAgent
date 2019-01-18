package de.wolff.paa.transform;

import java.io.DataOutput;
import java.io.IOException;

interface ClassPart {

  void writeTo(DataOutput sink) throws IOException;

}
