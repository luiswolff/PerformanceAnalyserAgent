package de.wolff.paa.stop;

import java.io.IOException;
import java.util.ResourceBundle;
import de.wolff.paa.AnalyserModule;

public class StopBeforeMainAnalyserModule implements AnalyserModule {

  @Override
  public void postStartJvm() {
    printBanner();
    waitForUserInput();
  }

  private void printBanner() {
    ResourceBundle bundle = ResourceBundle.getBundle("msg");
    System.out.println(bundle.getString("stop.hint"));
    System.out.println(bundle.getString("stop.pressButton"));
  }

  private void waitForUserInput() {
    try {
      System.in.read();
    } catch (IOException e) {
      throw new RuntimeException("Could not read from system input", e);
    }
  }

}
