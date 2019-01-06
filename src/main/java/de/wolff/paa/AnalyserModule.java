package de.wolff.paa;

public interface AnalyserModule {

  default void postStartJvm() {}

  default void preStopJvm() {}

}
