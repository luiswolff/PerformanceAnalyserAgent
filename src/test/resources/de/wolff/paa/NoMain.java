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
$ javap -c -p NoMain
Compiled from "NoMain.java"
public class NoMain {
  public NoMain();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void apply();
    Code:
       0: iconst_1
       1: istore_0
       2: iconst_2
       3: istore_1
       4: iload_0
       5: iload_1
       6: iadd
       7: istore_2
       8: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
      11: new           #3                  // class java/lang/StringBuilder
      14: dup
      15: invokespecial #4                  // Method java/lang/StringBuilder."<init>":()V
      18: ldc           #5                  // String Test calcualtion:
      20: invokevirtual #6                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      23: iload_2
      24: invokevirtual #7                  // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      27: invokevirtual #8                  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      30: invokevirtual #9                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      33: return
}
 * </pre></code>
 * 
 * <p>
 * After transformation the class definition should look similar to this:
 * </p>
 * <code><pre>
$ javap -c -p NoMain
Compiled from "NoMain.java"
public class NoMain {
  public NoMain();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void apply();
    Code:
       0: invokestatic  #2                  // Method de/wolff/paa/TestPerformanceAnalyserClassFileTransformer.invokationTargetByTransformation:()V
       3: iconst_1
       4: istore_0
       5: iconst_2
       6: istore_1
       7: iload_0
       8: iload_1
       9: iadd
      10: istore_2
      11: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
      14: new           #4                  // class java/lang/StringBuilder
      17: dup
      18: invokespecial #5                  // Method java/lang/StringBuilder."<init>":()V
      21: ldc           #6                  // String Test calcualtion:
      23: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      26: iload_2
      27: invokevirtual #8                  // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      30: invokevirtual #9                  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      33: invokevirtual #10                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      36: return
}
 * </pre></code>
 */
public class NoMain {

  public static void apply() {
    // de.wolff.paa.TestPerformanceAnalyserClassFileTransformer.invokationTargetByTransformation();
    int a = 1;
    int b = 2;
    int c = a + b;
    System.out.println("Test calcualtion: " + c);
  }

}
