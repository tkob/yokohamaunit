package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.util.SBuilder;

public class MockitoMockStrategy implements MockStrategy {

    private String mapArgumentType(Type argumentType) {
        int dims = argumentType.getDims();
        if (dims == 0) {
            return argumentType.getNonArrayType().accept(
                    primitiveType -> {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN: return "org.mockito.Mockito.anyBoolean()";
                            case BYTE:    return "org.mockito.Mockito.anyByte()";
                            case SHORT:   return "org.mockito.Mockito.anyShort()";
                            case INT:     return "org.mockito.Mockito.anyInt()";
                            case LONG:    return "org.mockito.Mockito.anyLong()";
                            case CHAR:    return "org.mockito.Mockito.anyChar()";
                            case FLOAT:   return "org.mockito.Mockito.anyFloat()";
                            case DOUBLE:  return "org.mockito.Mockito.anyDouble()";
                        }
                        throw new RuntimeException("should not reach here");
                    },
                    classType -> "org.mockito.Mockito.isA(" + classType.getName() + ".class)"
            );
        } else { 
            String brackets = StringUtils.repeat("[]", dims);
            return argumentType.getNonArrayType().accept(
                    primitiveType -> {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN: return "org.mockito.Mockito.isA(boolean" + brackets + ".class)";
                            case BYTE:    return "org.mockito.Mockito.isA(byte"    + brackets + ".class)";
                            case SHORT:   return "org.mockito.Mockito.isA(short"   + brackets + ".class)";
                            case INT:     return "org.mockito.Mockito.isA(int"     + brackets + ".class)";
                            case LONG:    return "org.mockito.Mockito.isA(long"    + brackets + ".class)";
                            case CHAR:    return "org.mockito.Mockito.isA(char"    + brackets + ".class)";
                            case FLOAT:   return "org.mockito.Mockito.isA(float"   + brackets + ".class)";
                            case DOUBLE:  return "org.mockito.Mockito.isA(double"  + brackets + ".class)";
                        }
                        throw new RuntimeException("should not reach here");
                    },
                    classType -> "org.mockito.Mockito.isA(" + classType.getName() + brackets + ".class)"
            );
        }
    }

    @Override
    public void stub(SBuilder sb, String name, StubExpr stubExpr, ExpressionStrategy expressionStrategy) {
        String classToStub = stubExpr.getClassToStub().getName();
        String fileName = stubExpr.getClassToStub().getSpan().getFileName();
        int startLine = stubExpr.getClassToStub().getSpan().getStart().getLine();
        String span = stubExpr.getClassToStub().getSpan().toString();
        sb.appendln(classToStub, " ", name, " = mock_(", classToStub, ".class",
                ", \"", escapeJava(fileName), "\", ",
                startLine,
                ", \"", escapeJava(span), "\");");
        for (StubBehavior behavior : stubExpr.getBehavior()) {
            MethodPattern methodPattern = behavior.getMethodPattern();
            String methodName = methodPattern.getName();
            boolean isVarArg = methodPattern.isVarArg();
            List<Type> argumentTypes = methodPattern.getArgumentTypes();
            String args =
                    isVarArg ? argumentTypes.subList(0, argumentTypes.size() - 1)
                                            .stream()
                                            .map(this::mapArgumentType)
                                            .collect(Collectors.joining(", "))
                               + (argumentTypes.size() > 1 ? ", org.mockito.Mockito.anyVararg()" : "org.mockito.Mockito.anyVararg()")
                             : argumentTypes.stream()
                                            .map(this::mapArgumentType)
                                            .collect(Collectors.joining(", "));
            behavior.getToBeReturned().<Void>accept(
                    quotedExpr -> {
                        String toBeReturned = expressionStrategy.getValue(quotedExpr);
                        sb.appendln("org.mockito.Mockito.when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", toBeReturned, ");");
                        return null;
                    },
                    stubExpr2 -> {
                        String name2 = name + "_";
                        sb.appendln("{");
                        sb.shift();
                        stub(sb, name2, stubExpr2, expressionStrategy);
                        sb.appendln("org.mockito.Mockito.when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", name2, ");");
                        sb.unshift();
                        sb.appendln("}");
                        return null;
                    },
                    matcherExpr -> {
                        String name2 = name + "_";
                        matcherExpr.getExpr(sb, name2, expressionStrategy, this);
                        sb.appendln("org.mockito.Mockito.when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", name2, ");");
                        return null;
                    },
                    newExpr -> {
                        String name2 = name + "_";
                        newExpr.getExpr(sb, name2);
                        sb.appendln("org.mockito.Mockito.when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", name2, ");");
                        return null;
                    }
            );
        }
    }

    @Override
    public void auxMethods(SBuilder sb) {
        sb.appendln("private <T> T mock_(Class<T> classToMock, String fileName, int startLine, String span) {");
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
