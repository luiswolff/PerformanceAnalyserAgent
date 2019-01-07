package de.wolff.paa;

import static org.mockito.Mockito.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPerformanceAnalyserClassFileTransformer {

  public static ModuleRunner moduleRunner;

  private CommandLineJavaCompiler javaCompiler;
  private TestClassLoader classLoaderOriginal;
  private TestClassLoader classLoaderRedefined;

  private PerformanceAnalyserClassFileTransformer classFileTransformer;

  @Before
  public void setUp() throws Exception {
    javaCompiler = new CommandLineJavaCompiler();
    classLoaderOriginal = new TestClassLoader();
    classLoaderRedefined = new TestClassLoader();

    classFileTransformer = new PerformanceAnalyserClassFileTransformer();
    classFileTransformer.setTargetClass(getClass());
    classFileTransformer.setTargetField("moduleRunner");
    classFileTransformer.setTargetMethod("jvmStart");
    moduleRunner = mock(ModuleRunner.class);
  }

  @After
  public void tearDown() throws Exception {
    moduleRunner = null;
  }

  @Test
  public void testTransformWithMain() throws Exception {
    URL javaWithMain = getClass().getResource("WithMain.java");
    Path compiled = javaCompiler.compile(javaWithMain);
    // TODO delete created class files

    classLoaderOriginal.addClass("WithMain", compiled);
    Class<?> clazz = classLoaderOriginal.loadClass("WithMain");

    byte[] redefined = classFileTransformer.transform(classLoaderOriginal, "WithMain", clazz,
        clazz.getProtectionDomain(), classLoaderOriginal.classDataFor("WithMain"));
    classLoaderRedefined.addClass("WithMain", redefined);

    clazz = classLoaderRedefined.loadClass("WithMain");
    Class<String[]> mainArgType = String[].class;
    Method main = clazz.getMethod("main", mainArgType);
    main.invoke(null, new Object[] {new String[0]});

    verify(moduleRunner).jvmStart();
  }



}

class CommandLineJavaCompiler {

  Path compile(URL url) throws URISyntaxException {
    Path path = Paths.get(url.toURI());
    try {
      Process process = new ProcessBuilder("javac", path.toString()).start();
      int exitCode = process.waitFor();
      if (exitCode == 0) {
        read(process.getInputStream(), System.out);
      } else {
        read(process.getErrorStream(), System.err);
      }
      return path.resolveSibling(path.getFileName().toString().replace(".java", ".class"));
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static void read(InputStream source, PrintStream sink) throws IOException {
    try (Reader in = new InputStreamReader(source)) {
      int c;
      while ((c = in.read()) != -1) {
        sink.print((char) c);
      }
    }
  }

}

class TestClassLoader extends ClassLoader {

  private final List<Class<?>> additionalClasses = new LinkedList<>();
  private final Map<String, byte[]> classDataMap = new HashMap<>();

  TestClassLoader() {
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

  void addClass(String className, Path classPath) {
    try {
      addClass(className, Files.readAllBytes(classPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void addClass(String className, byte[] classData) {
    Class<?> clazz = defineClass(className, classData, 0, classData.length);
    resolveClass(clazz);
    additionalClasses.add(clazz);
    classDataMap.put(className, classData);
  }

  byte[] classDataFor(String key) {
    return classDataMap.get(key);
  }

}
