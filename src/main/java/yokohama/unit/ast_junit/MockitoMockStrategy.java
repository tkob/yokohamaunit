package yokohama.unit.ast_junit;

import yokohama.unit.util.SBuilder;

public class MockitoMockStrategy implements MockStrategy {
    @Override
    public void auxMethods(SBuilder sb) {
        sb.appendln("private static <T> T mock_(Class<T> classToMock, String fileName, int startLine, String span) {");
        sb.shift();
            sb.appendln("try {");
            sb.shift();
                sb.appendln("return org.mockito.Mockito.mock(classToMock);");
            sb.unshift();
            sb.appendln("} catch (Exception e) {");
            sb.shift();
                sb.appendln("RuntimeException e2 = new RuntimeException(span + \" \" + e.getMessage(), e);");
                sb.appendln("StackTraceElement[] st = { new StackTraceElement(\"\", \"\", fileName, startLine) };");
                sb.appendln("e2.setStackTrace(st);");
                sb.appendln("throw e2;");
            sb.unshift();
            sb.appendln("}");
        sb.unshift();
        sb.appendln("}");
    }
}
