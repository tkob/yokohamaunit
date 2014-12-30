package yokohama.unit.ast_junit;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import yokohama.unit.ast.Kind;
import yokohama.unit.util.SBuilder;

public class MockitoMockStrategyTest {
    
    @RunWith(Theories.class)
    public static class Stub {
        @DataPoints
        public static Fixture[] PARAMs = {
            // no method definition
            new Fixture(
                    new StubExpr(new QuotedExpr("Object"), Arrays.asList()),
                    new SBuilder(4).appendln("Object stub = mock(Object.class);").toString()),
            // no arguments
            new Fixture(
                    new StubExpr(
                            new QuotedExpr("java.util.concurrent.Callable"),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern("call", Arrays.asList(), false),
                                            new QuotedExpr("42")))),
                    new SBuilder(4)
                            .appendln("java.util.concurrent.Callable stub = mock(java.util.concurrent.Callable.class);")
                            .appendln("when(stub.call()).thenReturn(Ognl.getValue(\"42\", env));")
                            .toString()),
            // one argument: class type
            new Fixture(
                    new StubExpr(
                            new QuotedExpr("Comparable"),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "compare",
                                                    Arrays.asList(
                                                            new Type(new ClassType("Object"), 0)
                                                    ),
                                                    false),
                                            new QuotedExpr("0")))),
                    new SBuilder(4)
                            .appendln("Comparable stub = mock(Comparable.class);")
                            .appendln("when(stub.compare(isA(Object.class))).thenReturn(Ognl.getValue(\"0\", env));")
                            .toString()),
            // one argument: primitive type
            new Fixture(
                    new StubExpr(
                            new QuotedExpr("CharSequence"),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "charAt",
                                                    Arrays.asList(
                                                            new Type(new PrimitiveType(Kind.INT), 0)
                                                    ),
                                                    false),
                                            new QuotedExpr("'a'")))),
                    new SBuilder(4)
                            .appendln("CharSequence stub = mock(CharSequence.class);")
                            .appendln("when(stub.charAt(anyInt())).thenReturn(Ognl.getValue(\"'a'\", env));")
                            .toString())
        };

        @Theory
        public void testStub(final Fixture fixture) {
            // Setup
            SBuilder sb = new SBuilder(4);
            String name = "stub";
            ExpressionStrategy expressionStrategy = new OgnlExpressionStrategy();
            MockitoMockStrategy instance = new MockitoMockStrategy();

            // Exercise
            instance.stub(sb, name, fixture.stubExpr, expressionStrategy);

            // Verify
            String actual = sb.toString();
            String expected = fixture.javaCode;
            assertThat(actual, is(expected));
        }

        @AllArgsConstructor
        public static class Fixture {
            public final StubExpr stubExpr;
            public final String javaCode;
        }
    }

    @RunWith(Theories.class)
    public static class StubTypes {
        @DataPoints
        public static Fixture[] PARAMs = {
            // non array types
            new Fixture(new PrimitiveType(Kind.BOOLEAN), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyBoolean())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.BYTE), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyByte())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.SHORT), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyShort())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.INT), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyInt())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.LONG), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyLong())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.CHAR), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyChar())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.FLOAT), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyFloat())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.DOUBLE), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyDouble())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new ClassType("Object"), 0, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(Object.class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            // array types
            new Fixture(new PrimitiveType(Kind.BOOLEAN), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(boolean[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.BYTE), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(byte[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.SHORT), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(short[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.INT), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(int[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.LONG), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(long[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.CHAR), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(char[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.FLOAT), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(float[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new PrimitiveType(Kind.DOUBLE), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(double[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new ClassType("Object"), 1, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(Object[].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            // array types 2D
            new Fixture(new PrimitiveType(Kind.BOOLEAN), 2, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(boolean[][].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString()),
            new Fixture(new ClassType("Object"), 2, new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(isA(Object[][].class))).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString())
        };

        @Theory
        public void testStub(final Fixture fixture) {
            // Setup
            SBuilder sb = new SBuilder(4);
            String name = "stub";
            ExpressionStrategy expressionStrategy = new OgnlExpressionStrategy();
            MockitoMockStrategy instance = new MockitoMockStrategy();

            // Exercise
            instance.stub(
                    sb,
                    name,
                    new StubExpr(
                            new QuotedExpr("DummyClass"),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "dummyMethod",
                                                    Arrays.asList(
                                                            new Type(fixture.type, fixture.dims)
                                                    ),
                                                    false),
                                            new QuotedExpr("null")))),
                    expressionStrategy);

            // Verify
            String actual = sb.toString();
            String expected = fixture.javaCode;
            assertThat(actual, is(expected));
        }

        @AllArgsConstructor
        public static class Fixture {
            public final NonArrayType type;
            public final int dims;
            public final String javaCode;
        }
    }

    @RunWith(Theories.class)
    public static class StubVararg {
        @DataPoints
        public static NonArrayType[] PARAMs = {
            new PrimitiveType(Kind.BOOLEAN),
            new PrimitiveType(Kind.BYTE),
            new PrimitiveType(Kind.SHORT),
            new PrimitiveType(Kind.INT),
            new PrimitiveType(Kind.LONG),
            new PrimitiveType(Kind.CHAR),
            new PrimitiveType(Kind.FLOAT),
            new PrimitiveType(Kind.DOUBLE),
            new ClassType("java.lang.Object")
        };

        @Theory
        public void testStub_VarargOnly(final NonArrayType fixture) {
            // Setup
            SBuilder sb = new SBuilder(4);
            String name = "stub";
            ExpressionStrategy expressionStrategy = new OgnlExpressionStrategy();
            MockitoMockStrategy instance = new MockitoMockStrategy();

            // Exercise
            instance.stub(
                    sb,
                    name,
                    new StubExpr(
                            new QuotedExpr("DummyClass"),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "dummyMethod",
                                                    Arrays.asList(
                                                            new Type(fixture, 0)
                                                    ),
                                                    true),
                                            new QuotedExpr("null")))),
                    expressionStrategy);

            // Verify
            String actual = sb.toString();
            String expected = new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyVararg())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString();
            assertThat(actual, is(expected));
        }

        @Theory
        public void testStub_ThereIsPrecedingArg(final NonArrayType fixture) {
            // Setup
            SBuilder sb = new SBuilder(4);
            String name = "stub";
            ExpressionStrategy expressionStrategy = new OgnlExpressionStrategy();
            MockitoMockStrategy instance = new MockitoMockStrategy();

            // Exercise
            instance.stub(
                    sb,
                    name,
                    new StubExpr(
                            new QuotedExpr("DummyClass"),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "dummyMethod",
                                                    Arrays.asList(
                                                            new Type(new PrimitiveType(Kind.INT), 0),
                                                            new Type(fixture, 0)
                                                    ),
                                                    true),
                                            new QuotedExpr("null")))),
                    expressionStrategy);

            // Verify
            String actual = sb.toString();
            String expected = new SBuilder(4)
                    .appendln("DummyClass stub = mock(DummyClass.class);")
                    .appendln("when(stub.dummyMethod(anyInt(), anyVararg())).thenReturn(Ognl.getValue(\"null\", env));")
                    .toString();
            assertThat(actual, is(expected));
        }
    }
}
