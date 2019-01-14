package de.wolff.paa;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPerformanceAnalyserClassFileTransformer {

  private static Consumer<?> moduleRunner;

  private List<Path> compiledClassFiles = new LinkedList<Path>();

  private final TemporaryClassLoader classLoaderOriginal = new TemporaryClassLoader();
  private final TemporaryClassLoader classLoaderRedefined = new TemporaryClassLoader();

  private PerformanceAnalyserClassFileTransformer classFileTransformer;

  private List<String> classNames;
  private List<String> classesForRedefine;
  private String redefineMethodName;
  private Class<?>[] redefineMethodParameterTypes;

  @Before
  public void setUp() throws Exception {
    moduleRunner = mock(Consumer.class);
  }

  @After
  public void tearDown() throws Exception {
    moduleRunner = null;
    for (Path compiledClassFile : compiledClassFiles) {
      Files.deleteIfExists(compiledClassFile);
    }
  }

  @Test
  public void testTransformWithMain() {
    classNames = Arrays.asList("WithMain");
    classesForRedefine = Collections.singletonList(classNames.get(0));
    redefineMethodName = "main";
    redefineMethodParameterTypes = new Class<?>[] {String[].class};

    compileClasses();
    fillClassLoaders();

    checkByteCodeEqualsExpect("WithMain");

    invokeStaticRedefined(classNames.get(0), redefineMethodName, redefineMethodParameterTypes,
        new Object[] {new String[0]});

    verify(moduleRunner).accept(null);
  }

  @Test
  public void testNoTransformWithMain() {
    classNames = Arrays.asList("WithMain");
    classesForRedefine = Collections.singletonList(classNames.get(0));
    redefineMethodName = "apply";
    redefineMethodParameterTypes = new Class<?>[] {};

    compileClasses();
    fillClassLoaders();

    checkByteCodeEqualsExpect();
  }

  @Test
  public void testTransformNoMain() {
    classNames = Arrays.asList("NoMain");
    classesForRedefine = Collections.singletonList(classNames.get(0));
    redefineMethodName = "apply";
    redefineMethodParameterTypes = new Class<?>[] {};

    compileClasses();
    fillClassLoaders();

    checkByteCodeEqualsExpect("NoMain");

    invokeStaticRedefined(classNames.get(0), "apply", new Class<?>[] {});

    verify(moduleRunner).accept(null);
  }

  @Test
  public void testNoTransformNoMain() {
    classNames = Arrays.asList("NoMain");
    classesForRedefine = Collections.singletonList(classNames.get(0));
    redefineMethodName = "main";
    redefineMethodParameterTypes = new Class<?>[] {String[].class};

    compileClasses();
    fillClassLoaders();

    checkByteCodeEqualsExpect();
  }

  private void compileClasses() {
    CommandLineJavaCompiler javaCompiler = new CommandLineJavaCompiler("UTF-8", "1.8");
    classNames.stream().map(this::toJavaFile).map(javaCompiler::compile)
        .forEach(compiledClassFiles::add);
  }

  private Path toJavaFile(String javaFileName) {
    try {
      URI javaFileUri = getClass().getResource(javaFileName + ".java").toURI();
      return Paths.get(javaFileUri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private void fillClassLoaders() {
    createClassFileTransformer();

    fill(classLoaderOriginal);
    classesForRedefine.forEach(this::redefine);
    fill(classLoaderRedefined);
  }

  private void createClassFileTransformer() {
    classFileTransformer = new PerformanceAnalyserClassFileTransformer();
    classFileTransformer.setInvocationClass(getClass());
    classFileTransformer.setInvocationMethod("invokationTargetByTransformation");
    classFileTransformer.setRedefineMethodModifiers(Modifier.PUBLIC + Modifier.STATIC);
    classFileTransformer.setRedefineMethodName(redefineMethodName);
    classFileTransformer.setRedefineMathodParameterTypes(redefineMethodParameterTypes);
  }

  private void fill(TemporaryClassLoader classLoader) {
    compiledClassFiles.forEach(classLoader::addClassFile);
  }

  private void redefine(String className) {
    try {
      Class<?> clazz = classLoaderOriginal.loadClass(className);
      byte[] classData = classLoaderOriginal.classData(className);

      classData = classFileTransformer.transform(classLoaderOriginal, className, clazz,
          clazz.getProtectionDomain(), classData);
      classLoaderRedefined.addClass(className, classData);
    } catch (ClassNotFoundException | IllegalClassFormatException e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeStaticRedefined(String className, String methodName, Class<?>[] parameterTypes,
      Object... args) {

    try {
      Class<?> clazz = classLoaderRedefined.loadClass(className);
      Method method = clazz.getMethod(methodName, parameterTypes);
      method.invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void checkByteCodeEqualsExpect(String... redefined) {
    List<String> redefinedList = Arrays.asList(redefined);

    for (String className : classNames) {
      byte[] originalClass = classLoaderOriginal.classData(className);
      byte[] redefinedClass = classLoaderRedefined.classData(className);

      boolean classesEquals = Arrays.equals(originalClass, redefinedClass);
      if (redefinedList.contains(className)) {
        assertFalse("Class " + className + " is expected to be redefined but wasn't",
            classesEquals);
      } else {
        assertTrue("Class " + className + " is expeced to be unmodified but was", classesEquals);
      }
    }
  }

  public static void invokationTargetByTransformation() {
    // execute method on static field, so the JUnit test instance can verify whether the redefined
    // class has executed this method
    moduleRunner.accept(null);
  }
}


class CommandLineJavaCompiler {

  private final String[] javac;
  private final int commandLength;

  CommandLineJavaCompiler(String encoding, String javaVersion) {
    javac = new String[] {"javac", "-encoding", encoding, "-source", javaVersion, "-target",
        javaVersion};
    commandLength = javac.length;
  }

  Path compile(Path javaFile) {
    executeCompiler(javaFile.toString());

    Path classFile = toClassFile(javaFile);
    System.out.printf("Compiled file %s to %s", javaFile, classFile);
    System.out.println();

    if (Files.notExists(classFile)) {
      throw new RuntimeException("Could not find class file " + classFile);
    }
    return classFile;
  }

  private void executeCompiler(String javaFile) {
    String[] command = Arrays.copyOf(javac, commandLength + 1);
    command[commandLength] = javaFile;

    System.out.printf("Execute Compiler with %s", Arrays.toString(command));
    System.out.println();

    try {
      Process javacProcess = new ProcessBuilder(command).inheritIO().start();
      int exitCode = javacProcess.waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("Java compliers exists with exit code " + exitCode);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private Path toClassFile(Path path) {
    String javaFileName = path.getFileName().toString();
    String classFileName = javaFileName.replace(".java", ".class");
    return path.resolveSibling(classFileName);
  }

}


class TemporaryClassLoader extends ClassLoader {

  private final List<Class<?>> additionalClasses = new LinkedList<>();
  private final Map<String, byte[]> classDataMap = new HashMap<>();

  TemporaryClassLoader() {
    super(ClassLoader.getSystemClassLoader());
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

    System.out.printf("ClassLoader %s adds class %s", this, className);
    System.out.println();

    Class<?> clazz = defineClass(className, classData, 0, classData.length);
    resolveClass(clazz);
    additionalClasses.add(clazz);
    classDataMap.put(className, classData);
  }

  byte[] classData(String key) {
    return classDataMap.get(key);
  }

}
