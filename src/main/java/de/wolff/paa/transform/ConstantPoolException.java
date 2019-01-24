package de.wolff.paa.transform;

class ConstantPoolException extends RuntimeException {

  private static final long serialVersionUID = 4736050301676198767L;

  ConstantPoolException(int index, String expectedType) {
    super(String.format("Entry at constant pool index %d is expected to be of type %s", index,
        expectedType));
  }

  ConstantPoolException(String expectedEntry) {
    super(String.format("Expected constant pool entry %s is missing", expectedEntry));
  }

}
