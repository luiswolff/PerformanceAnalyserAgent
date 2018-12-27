package de.wolff.paa;

import java.lang.instrument.Instrumentation;

public class PerformanceAnalyserAgent {

  public static void premain(String args, Instrumentation instrumentation) {
    System.out.println("Hello world from javaagent");
  }

}
