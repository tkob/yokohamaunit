package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class VarInitStatement implements Statement {
    private final Type type;
    private final String name;
    private final Expr value;
    private final Span span;

    @Override
    public void toString(SBuilder sb) {
        value.<Void>accept(
                varExpr -> {
                    sb.appendln(name, " = ", varExpr.getName(), ";");
                    return null;
                },
                instanceOfMatcherExpr -> {
                    instanceOfMatcherExpr.getExpr(sb, name);
                    return null;
                },
                nullValueMatcherExpr -> {
                    nullValueMatcherExpr.getExpr(sb, name);
                    return null;
                },
                equalToMatcherExpr -> {
                    equalToMatcherExpr.getExpr(sb, name);
                    return null;
                },
                newExpr -> {
                    newExpr.getExpr(sb, name);
                    return null;
                },
                strLitExpr -> {
                    sb.appendln(name, " = \"", escapeJava(strLitExpr.getText()), "\";");
                    return null;
                },
                nullExpr -> {
                    sb.appendln(name, " = null;");
                    return null;
                },
                invokeExpr -> {
                    invokeExpr.getExpr(sb, type, name);
                    return null;
                },
                invokeStaticExpr -> {
                    invokeStaticExpr.getExpr(sb, type, name);
                    return null;
                },
                intLitExpr -> {
                    sb.appendln(name, " = ", intLitExpr.getValue(), ";");
                    return null;
                },
                classLitExpr -> {
                    sb.appendln(name, " = ", classLitExpr.getType().getText(), ".class;");
                    return null;
                },
                equalOpExpr -> {
                    sb.appendln(name, " = ", equalOpExpr.getLhs().getName(), " == ", equalOpExpr.getRhs().getName(), ";");
                    return null;
                });
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarInitStatement(this);
    }
}
