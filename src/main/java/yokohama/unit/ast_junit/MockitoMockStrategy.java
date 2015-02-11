package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;

public class MockitoMockStrategy implements MockStrategy {

    private String mapArgumentType(Type argumentType) {
        int dims = argumentType.getDims();
        if (dims == 0) {
            return argumentType.getNonArrayType().accept(
                    primitiveType -> {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN: return "anyBoolean()";
                            case BYTE:    return "anyByte()";
                            case SHORT:   return "anyShort()";
                            case INT:     return "anyInt()";
                            case LONG:    return "anyLong()";
                            case CHAR:    return "anyChar()";
                            case FLOAT:   return "anyFloat()";
                            case DOUBLE:  return "anyDouble()";
                        }
                        throw new RuntimeException("should not reach here");
                    },
                    classType -> "isA(" + classType.getName() + ".class)"
            );
        } else { 
            String brackets = StringUtils.repeat("[]", dims);
            return argumentType.getNonArrayType().accept(
                    primitiveType -> {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN: return "isA(boolean" + brackets + ".class)";
                            case BYTE:    return "isA(byte"    + brackets + ".class)";
                            case SHORT:   return "isA(short"   + brackets + ".class)";
                            case INT:     return "isA(int"     + brackets + ".class)";
                            case LONG:    return "isA(long"    + brackets + ".class)";
                            case CHAR:    return "isA(char"    + brackets + ".class)";
                            case FLOAT:   return "isA(float"   + brackets + ".class)";
                            case DOUBLE:  return "isA(double"  + brackets + ".class)";
                        }
                        throw new RuntimeException("should not reach here");
                    },
                    classType -> "isA(" + classType.getName() + brackets + ".class)"
            );
        }
    }

    private Set<ImportedName> argumentTypeImports(Type argumentType) {
        if (argumentType.getDims() > 0) {
            return setOf(new ImportStatic("org.mockito.Mockito.isA"));
        } else {
            return argumentType.getNonArrayType().accept(
                    primitiveType -> {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN: return setOf(new ImportStatic("org.mockito.Mockito.anyBoolean"));
                            case BYTE:    return setOf(new ImportStatic("org.mockito.Mockito.anyByte"));
                            case SHORT:   return setOf(new ImportStatic("org.mockito.Mockito.anyShort"));
                            case INT:     return setOf(new ImportStatic("org.mockito.Mockito.anyInt"));
                            case LONG:    return setOf(new ImportStatic("org.mockito.Mockito.anyLong"));
                            case CHAR:    return setOf(new ImportStatic("org.mockito.Mockito.anyChar"));
                            case FLOAT:   return setOf(new ImportStatic("org.mockito.Mockito.anyFloat"));
                            case DOUBLE:  return setOf(new ImportStatic("org.mockito.Mockito.anyDouble"));
                        }
                        throw new RuntimeException("should not reach here");
                    },
                    classType -> setOf(new ImportStatic("org.mockito.Mockito.isA"))
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
                               + (argumentTypes.size() > 1 ? ", anyVararg()" : "anyVararg()")
                             : argumentTypes.stream()
                                            .map(this::mapArgumentType)
                                            .collect(Collectors.joining(", "));
            behavior.getToBeReturned().<Void>accept(
                    quotedExpr -> {
                        String toBeReturned = expressionStrategy.getValue(quotedExpr);
                        sb.appendln("when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", toBeReturned, ");");
                        return null;
                    },
                    stubExpr2 -> {
                        String name2 = name + "_";
                        sb.appendln("{");
                        sb.shift();
                        stub(sb, name2, stubExpr2, expressionStrategy);
                        sb.appendln("when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", name2, ");");
                        sb.unshift();
                        sb.appendln("}");
                        return null;
                    },
                    matcherExpr -> {
                        String name2 = name + "_";
                        matcherExpr.getExpr(sb, name2, expressionStrategy, this);
                        sb.appendln("when((Object)", name, ".", methodName, "(", args, ")).thenReturn(", name2, ");");
                        return null;
                    }
            );
        }
    }

    @Override
    public Set<ImportedName> stubImports(StubExpr stubExpr, ExpressionStrategy expressionStrategy) {
        return stubExpr.getBehavior()
                .stream()
                .collect(
                        () -> setOf(new ImportStatic("org.mockito.Mockito.mock")),
                        (set, behavior) -> {
                            MethodPattern methodPattern = behavior.getMethodPattern();
                            boolean isVarArg = methodPattern.isVarArg();
                            List<Type> argumentTypes = methodPattern.getArgumentTypes();
                            Expr toBeReturned = behavior.getToBeReturned();

                            if (isVarArg) {
                                set.add(new ImportStatic("org.mockito.Mockito.anyVararg"));
                                set.addAll(argumentTypes.subList(0, argumentTypes.size() - 1).stream()
                                        .collect(
                                                () -> new TreeSet<>(),
                                                (set_, type) -> set_.addAll(argumentTypeImports(type)),
                                                (s1, s2) -> s1.addAll(s2)));
                            } else {
                                set.addAll(argumentTypes.stream()
                                        .collect(
                                                () -> new TreeSet<>(),
                                                (set_, type) -> set_.addAll(argumentTypeImports(type)),
                                                (s1, s2) -> s1.addAll(s2)));
                            }

                            set.add(new ImportStatic("org.mockito.Mockito.when"));
                            toBeReturned.<Void>accept(
                                    quotedExpr -> {
                                        set.addAll(expressionStrategy.getValueImports());
                                        return null;
                                    },
                                    (StubExpr stubExpr_) -> {
                                        set.addAll(stubImports(stubExpr, expressionStrategy));
                                        return null;
                                    },
                                    matcherExpr -> {
                                        set.addAll(matcherExpr.importedNames(expressionStrategy, this));
                                        return null;
                                    }
                            );

                        },
                        (s1, s2) -> s1.addAll(s2)
                );
    }

    @Override
    public void auxMethods(SBuilder sb) {
        sb.appendln("private <T> T mock_(Class<T> classToMock, String fileName, int startLine, String span) {");
        sb.shift();
            sb.appendln("try {");
            sb.shift();
                sb.appendln("return mock(classToMock);");
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

    @Override
    public Set<ImportedName> auxMethodsImports() {
        return setOf(new ImportStatic("org.mockito.Mockito.mock"));
    }

}
