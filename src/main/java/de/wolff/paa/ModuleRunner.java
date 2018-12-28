package de.wolff.paa;

import java.util.LinkedList;

class ModuleRunner {
  
  private final LinkedList<AnalyserModule> modules = new LinkedList<>();
  private final LinkedList<AnalyserModule> reverted = new LinkedList<>();

  ModuleRunner(Iterable<AnalyserModule> modules) {
    modules.forEach(this.modules::addLast);
    modules.forEach(this.reverted::addFirst);
  }

  void jvmStart() {
    modules.forEach(AnalyserModule::postStartJvm);
  }

  void jvmStop() {
    reverted.forEach(AnalyserModule::preStopJvm);
  }

}
