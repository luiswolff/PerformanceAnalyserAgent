package de.wolff.paa;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.ServiceLoader;
import de.wolff.paa.transform.MethodInvokedCallbackTransformer;

public class PerformanceAnalyserAgent {

  public static ModuleRunner moduleRunner;

  public static void premain(String args, Instrumentation instrumentation) {
    moduleRunner = createModuleRunner();
    instrumentation.addTransformer(createMethodInvokedCallbackTransformer(), true);
    addShutdownHook(moduleRunner::jvmStop);
  }

  private static MethodInvokedCallbackTransformer createMethodInvokedCallbackTransformer() {
    MethodInvokedCallbackTransformer classFileTransformer = new MethodInvokedCallbackTransformer();
    classFileTransformer.setInvocationClass(PerformanceAnalyserAgent.class);
    classFileTransformer.setInvocationMethod("mainInvoked");
    classFileTransformer.setRedefineMethodModifiers(Modifier.PUBLIC + Modifier.STATIC);
    classFileTransformer.setRedefineMethodName("main");
    classFileTransformer.setRedefineMathodParameterTypes(new Class<?>[] {String[].class});
    return classFileTransformer;
  }

  public static void mainInvoked() {
    moduleRunner.jvmStart();
  }

  private static ModuleRunner createModuleRunner() {
    ServiceLoader<AnalyserModule> analyserModules = ServiceLoader.load(AnalyserModule.class);
    return new ModuleRunner(analyserModules);
  }

  private static void addShutdownHook(Runnable target) {
    Thread shutdownHook = new Thread(target);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

}
