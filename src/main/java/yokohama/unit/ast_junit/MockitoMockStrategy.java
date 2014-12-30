package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.util.SBuilder;

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

    @Override
    public void stub(SBuilder sb, String name, StubExpr stubExpr, ExpressionStrategy expressionStrategy) {
        String classToStub = stubExpr.getClassToStub().getText();
        sb.appendln(classToStub, " ", name, "= mock(", classToStub, ".class);");
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
                               + ", anyVararg()"
                             : argumentTypes.stream()
                                            .map(this::mapArgumentType)
                                            .collect(Collectors.joining(", "));
            behavior.getToBeReturned().<Void>accept(
                    quotedExpr -> {
                        String toBeReturned = expressionStrategy.getValue(quotedExpr.getText());
                        sb.appendln("when(", name, ".", methodName, "(", args, ")).thenReturn(", toBeReturned, ");");
                        return null;
                    },
                    stubExpr2 -> {
                        String name2 = name + "_";
                        sb.appendln("{");
                        sb.shift();
                        stub(sb, name2, stubExpr2, expressionStrategy);
                        sb.appendln("when(", name, ".", methodName, "(", args, ")).thenReturn(", name2, ");");
                        sb.unshift();
                        sb.appendln("}");
                        return null;
                    }
            );
        }
    }
    
}
