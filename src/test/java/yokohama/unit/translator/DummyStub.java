package yokohama.unit.translator;

/**
 * DummyStub interafce is instended to be used from TestStubVariations.docy,
 * to test stubbing with various argument/return types. 
 * 
 * It could be said that the variation below is incomplete, because varargs
 * other than Object..., e.g. int..., are missing.
 * 
 * This omission is due to OGNL limitations:
 * It seems that OGNL supports calling vararg methods only if the type is Obect.
 *
 */
public interface DummyStub {
    boolean get(boolean arg);
    byte get(byte arg);
    short get(short arg);
    int get(int arg);
    long get(long arg);
    char get(char arg);
    float get(float arg);
    double get(double arg);
    String get(String arg);
    boolean[] get(boolean[] arg);
    byte[] get(byte[] arg);
    short[] get(short[] arg);
    int[] get(int[] arg);
    long[] get(long[] arg);
    char[] get(char[] arg);
    float[] get(float[] arg);
    double[] get(double[] arg);
    String[] get(String[] arg);
    boolean[][] get(boolean[][] arg);
    byte[][] get(byte[][] arg);
    short[][] get(short[][] arg);
    int[][] get(int[][] arg);
    long[][] get(long[][] arg);
    char[][] get(char[][] arg);
    float[][] get(float[][] arg);
    double[][] get(double[][] arg);
    String[][] get(String[][] arg);

    Object getVararg(Object... vararg);
    int getVararg(int arg, Object... vararg);

    boolean[][] boolean2d = {};
    byte[][] byte2d = {};
    short[][] short2d = {};
    int[][] int2d = {};
    long[][] long2d = {};
    char[][] char2d = {};
    float[][] float2d = {};
    double[][] double2d = {};
    String[][] string2d = {};

}
