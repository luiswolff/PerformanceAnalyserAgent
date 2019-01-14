package de.wolff.paa;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class PerformanceAnalyserAgent {

  public static ModuleRunner moduleRunner;

  public static void premain(String args, Instrumentation instrumentation) {
    moduleRunner = createModuleRunner();
    instrumentation.addTransformer(new PerformanceAnalyserClassFileTransformer(), true);
    addShutdownHook(moduleRunner::jvmStop);
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
