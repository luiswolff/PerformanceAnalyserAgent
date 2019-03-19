package de.wolff.paa.transform;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class TestMethodInvokedCallbackTransformer {

  private static Runnable moduleRunner;

  private List<Path> compiledClassFiles = new LinkedList<Path>();

  private final TemporaryClassLoader classLoaderOriginal = new TemporaryClassLoader();
  private final TemporaryClassLoader classLoaderRedefined = new TemporaryClassLoader();

  private final List<Runnable> afterInvocation = new LinkedList<Runnable>();

  private MethodInvokedCallbackTransformer classFileTransformer;

  private List<String> classNames;
  private List<String> classesForRedefine;
  private String redefineMethodName;
  private Class<?>[] redefineMethodParameterTypes;

  @Before
  public void setUp() throws Exception {
    moduleRunner = mock(Runnable.class);
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
    classesForRedefine = Arrays.asList("WithMain");
    redefineMethodName = "main";
    redefineMethodParameterTypes = new Class<?>[] {String[].class};

    compileClasses();
    fillClassLoaders();

    checkByteCodeEqualsExpect("WithMain");

    setStaticRedefined("WithMain", "callback");
    invokeStaticRedefined("WithMain", redefineMethodName, redefineMethodParameterTypes,
        new Object[] {new String[0]});

    verifyInvocations();
  }

  @Test
  public void testTransformNoArgs() {
    classNames = Arrays.asList("NoMain");
    classesForRedefine = Arrays.asList("NoMain");
    redefineMethodName = "apply";
    redefineMethodParameterTypes = new Class<?>[] {};

    compileClasses();
    fillClassLoaders();

    setStaticRedefined("NoMain", "callback");
    checkByteCodeEqualsExpect("NoMain");

    invokeStaticRedefined("NoMain", "apply", new Class<?>[] {});

    verifyInvocations();
  }

  @Test
  public void testNoTransformBecauseWrongMethodName() {
    classNames = Arrays.asList("WithMain");
    classesForRedefine = Collections.singletonList(classNames.get(0));
    redefineMethodName = "apply";
    redefineMethodParameterTypes = new Class<?>[] {String[].class};
  
    compileClasses();
    fillClassLoaders();
  
    checkByteCodeEqualsExpect();
  }

  @Test
  public void testNoTransformBecauseNoArgsExpected() {
    classNames = Arrays.asList("WithMain");
    classesForRedefine = Arrays.asList("WithMain");
    redefineMethodName = "main";
    redefineMethodParameterTypes = new Class<?>[0];

    compileClasses();
    fillClassLoaders();

    checkByteCodeEqualsExpect();
  }

  private void compileClasses() {
    // TODO: add test cases with Java 6 compiled classes, because they do not use StackMapTables
    CommandLineJavaCompiler javaCompiler = new CommandLineJavaCompiler("UTF-8", "1.6");
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
    classFileTransformer = new MethodInvokedCallbackTransformer();
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

  private void setStaticRedefined(String className, String fieldName) {
    
    Runnable mockInvokation = mock(Runnable.class);
    try {
      Class<?> clazz = classLoaderRedefined.loadClass(className);
      Field field = clazz.getField(fieldName);
      field.set(null, mockInvokation);
      afterInvocation.add(mockInvokation);
    } catch (Exception e) {
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

  private void verifyInvocations() {

    InOrder inOrder = inOrder(allMocks());

    inOrder.verify(moduleRunner).run();
    afterInvocation.forEach(r -> inOrder.verify(r).run());
    inOrder.verifyNoMoreInteractions();
  }

  private Object[] allMocks() {
    List<Runnable> allMocks = new ArrayList<Runnable>(afterInvocation.size() + 1);
    allMocks.addAll(afterInvocation);
    allMocks.add(moduleRunner);
    return allMocks.toArray();
  }

  public static void invokationTargetByTransformation() {
    // execute method on static field, so the JUnit test instance can verify whether the redefined
    // class has executed this method
    moduleRunner.run();
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
