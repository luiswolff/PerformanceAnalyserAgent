/**
 * <p>
 * ATTENTION: Don't move this file to src/test/java. It will be compiled at runtime.
 * </p>
 * 
 * <p>
 * After compilation the class definition looks like:
 * </p>
 * 
 * <code><pre>
$ javap -c -p WithMain
Compiled from "WithMain.java"
public class WithMain {
  public static java.lang.Runnable callback;

  public WithMain();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: getstatic     #2                  // Field callback:Ljava/lang/Runnable;
       3: ifnull        14
       6: getstatic     #2                  // Field callback:Ljava/lang/Runnable;
       9: invokeinterface #3,  1            // InterfaceMethod java/lang/Runnable.run:()V
      14: return
}

 * </pre></code>
 * 
 * <p>
 * After transformation the class definition should look similar to this:
 * </p>
 * <code><pre>
$ javap -c -p WithMain
Compiled from "WithMain.java"
public class WithMain {
  public static java.lang.Runnable callback;

  public WithMain();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: invokestatic  #2                  // Method de/wolff/paa/TestPerformanceAnalyserClassFileTransformer.invokationTargetByTransformation:()V
       3: getstatic     #3                  // Field callback:Ljava/lang/Runnable;
       6: ifnull        17
       9: getstatic     #3                  // Field callback:Ljava/lang/Runnable;
      12: invokeinterface #4,  1            // InterfaceMethod java/lang/Runnable.run:()V
      17: return
}

 * </pre></code>
 */
public class WithMain {
  
  public static Runnable callback;

  public static void main(String[] args) {
    // de.wolff.paa.TestPerformanceAnalyserClassFileTransformer.invokationTargetByTransformation();
    if (callback != null) {
      callback.run();
    }
  }

}
