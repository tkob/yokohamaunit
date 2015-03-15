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
                    new StubExpr(new ClassType("Object", Span.dummySpan()), Arrays.asList()),
                    new SBuilder(4).appendln("stub = mock_(Object.class, \"?\", -1, \"?:?\");").toString()),
            // no arguments
            new Fixture(
                    new StubExpr(
                            new ClassType("java.util.concurrent.Callable", Span.dummySpan()),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern("call", Arrays.asList(), false),
                                            new Var("returned")))),
                    new SBuilder(4)
                            .appendln("stub = mock_(java.util.concurrent.Callable.class, \"?\", -1, \"?:?\");")
                            .appendln("org.mockito.Mockito.when((Object)stub.call()).thenReturn(returned);")
                            .toString()),
            // one argument: class type
            new Fixture(
                    new StubExpr(
                            new ClassType("Comparable", Span.dummySpan()),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "compare",
                                                    Arrays.asList(
                                                            new Type(new ClassType("Object", Span.dummySpan()), 0)
                                                    ),
                                                    false),
                                            new Var("returned")))),
                    new SBuilder(4)
                            .appendln("stub = mock_(Comparable.class, \"?\", -1, \"?:?\");")
                            .appendln("org.mockito.Mockito.when((Object)stub.compare(org.mockito.Mockito.isA(Object.class))).thenReturn(returned);")
                            .toString()),
            // one argument: primitive type
            new Fixture(
                    new StubExpr(
                            new ClassType("CharSequence", Span.dummySpan()),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "charAt",
                                                    Arrays.asList(
                                                            new Type(new PrimitiveType(Kind.INT), 0)
                                                    ),
                                                    false),
                                            new Var("returned")))),
                    new SBuilder(4)
                            .appendln("stub = mock_(CharSequence.class, \"?\", -1, \"?:?\");")
                            .appendln("org.mockito.Mockito.when((Object)stub.charAt(org.mockito.Mockito.anyInt())).thenReturn(returned);")
                            .toString()),
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
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyBoolean())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.BYTE), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyByte())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.SHORT), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyShort())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.INT), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyInt())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.LONG), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyLong())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.CHAR), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyChar())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.FLOAT), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyFloat())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.DOUBLE), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyDouble())).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new ClassType("Object", Span.dummySpan()), 0, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(Object.class))).thenReturn(__);")
                    .toString()
            ),
            // array types
            new Fixture(new PrimitiveType(Kind.BOOLEAN), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(boolean[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.BYTE), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(byte[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.SHORT), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(short[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.INT), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(int[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.LONG), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(long[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.CHAR), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(char[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.FLOAT), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(float[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new PrimitiveType(Kind.DOUBLE), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(double[].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new ClassType("Object", Span.dummySpan()), 1, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(Object[].class))).thenReturn(__);")
                    .toString()
            ),
            // array types 2D
            new Fixture(new PrimitiveType(Kind.BOOLEAN), 2, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(boolean[][].class))).thenReturn(__);")
                    .toString()
            ),
            new Fixture(new ClassType("Object", Span.dummySpan()), 2, new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.isA(Object[][].class))).thenReturn(__);")
                    .toString()
            ),
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
                            new ClassType("DummyClass", Span.dummySpan()),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "dummyMethod",
                                                    Arrays.asList(
                                                            new Type(fixture.type, fixture.dims)
                                                    ),
                                                    false),
                                            new Var("__")))),
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
            new ClassType("java.lang.Object", Span.dummySpan())
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
                            new ClassType("DummyClass", Span.dummySpan()),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "dummyMethod",
                                                    Arrays.asList(
                                                            new Type(fixture, 0)
                                                    ),
                                                    true),
                                            new Var("__")))),
                    expressionStrategy);

            // Verify
            String actual = sb.toString();
            String expected = new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyVararg())).thenReturn(__);")
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
                            new ClassType("DummyClass", Span.dummySpan()),
                            Arrays.asList(
                                    new StubBehavior(
                                            new MethodPattern(
                                                    "dummyMethod",
                                                    Arrays.asList(
                                                            new Type(new PrimitiveType(Kind.INT), 0),
                                                            new Type(fixture, 0)
                                                    ),
                                                    true),
                                            new Var("__")))),
                    expressionStrategy);

            // Verify
            String actual = sb.toString();
            String expected = new SBuilder(4)
                    .appendln("stub = mock_(DummyClass.class, \"?\", -1, \"?:?\");")
                    .appendln("org.mockito.Mockito.when((Object)stub.dummyMethod(org.mockito.Mockito.anyInt(), org.mockito.Mockito.anyVararg())).thenReturn(__);")
                    .toString();
            assertThat(actual, is(expected));
        }
    }
}
