package de.wolff.paa;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

class ModuleRunner {
  
  private final Deque<AnalyserModule> modules = new LinkedList<>();

  ModuleRunner(Iterable<AnalyserModule> modules) {
    modules.forEach(this.modules::addLast);
  }

  void jvmStart() {
    forEach(modules.iterator(), AnalyserModule::postStartJvm);
  }

  void jvmStop() {
    forEach(modules.descendingIterator(), AnalyserModule::preStopJvm);
  }

  private static void forEach(Iterator<AnalyserModule> modules, Consumer<AnalyserModule> action) {
    modules.forEachRemaining(action);
  }

}
