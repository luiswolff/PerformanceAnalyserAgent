package de.wolff.paa.transform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TemporaryClassLoader extends ClassLoader {

  private final List<Class<?>> additionalClasses = new LinkedList<>();
  private final Map<String, byte[]> classDataMap = new HashMap<>();

  TemporaryClassLoader() {
    super(TemporaryClassLoader.class.getClassLoader());
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    Optional<Class<?>> clazz =
        additionalClasses.stream().filter(c -> name.equals(c.getName())).findFirst();
    if (clazz.isPresent()) {
      return clazz.get();
    }
    return super.loadClass(name);
  }

  void addClassFile(Path classFile) {
    String classFileName = classFile.getFileName().toString();
    try {
      addClass(classFileName.replace(".class", ""), Files.readAllBytes(classFile));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void addClass(String className, byte[] classData) {
    if (classDataMap.containsKey(className)) {
      return;
    }

    Class<?> clazz = defineClass(className, classData, 0, classData.length);
    resolveClass(clazz);
    additionalClasses.add(clazz);
    classDataMap.put(className, classData);
  }

  byte[] classData(String key) {
    return classDataMap.get(key);
  }

}