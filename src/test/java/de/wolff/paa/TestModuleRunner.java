package de.wolff.paa;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.mockito.InOrder;

public class TestModuleRunner {

  private List<AnalyserModule> mockAnalyserModules = new LinkedList<AnalyserModule>();
  private InOrder inOrder;
  private ModuleRunner moduleRunner;

  @Test
  public void testInvokeOrder() {
    modules(2);

    postJvmStartInvokations(0, 1);
    preJvmStopInvokations(1, 0);
  }

  private void modules(int count) {
    for (int i = 0; i < count; i++) {
      mockAnalyserModules.add(mock(AnalyserModule.class));
    }

    inOrder = inOrder(mockAnalyserModules.toArray());
    moduleRunner = new ModuleRunner(mockAnalyserModules);
  }

  private void postJvmStartInvokations(Integer... indices) {
    moduleRunner.jvmStart();

    Arrays.stream(indices).map(mockAnalyserModules::get).forEach(this::verifyPostStartInvoke);
  }

  private void verifyPostStartInvoke(AnalyserModule mockAnalyserModule) {
    inOrder.verify(mockAnalyserModule).postStartJvm();
  }

  private void preJvmStopInvokations(Integer... indices) {
    moduleRunner.jvmStop();

    Arrays.stream(indices).map(mockAnalyserModules::get).forEach(this::verifyPreJvmInvoke);
    
  }

  private void verifyPreJvmInvoke(AnalyserModule mockAnalyserModule) {
    inOrder.verify(mockAnalyserModule).preStopJvm();
  }

}
