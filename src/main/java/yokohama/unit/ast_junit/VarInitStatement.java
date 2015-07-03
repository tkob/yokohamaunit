package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.position.Span;
import yokohama.unit.util.SBuilder;
import yokohama.unit.util.Sym;

@Value
public class VarInitStatement implements Statement {
    private final Type type;
    private final Sym var;
    private final Expr value;
    private final Span span;

    @Override
    public void toString(SBuilder sb) {
        value.<Void>accept(
                varExpr -> {
                    sb.appendln(var.getName(), " = ", varExpr.getVar().getName(), ";");
                    return null;
                },
                instanceOfMatcherExpr -> {
                    instanceOfMatcherExpr.getExpr(sb, var.getName());
                    return null;
                },
                nullValueMatcherExpr -> {
                    nullValueMatcherExpr.getExpr(sb, var.getName());
                    return null;
                },
                equalToMatcherExpr -> {
                    equalToMatcherExpr.getExpr(sb, var.getName());
                    return null;
                },
                regExpMatcherExpr -> {
                    regExpMatcherExpr.getExpr(sb, var.getName());
                    return null;
                },
                newExpr -> {
                    newExpr.getExpr(sb, var.getName());
                    return null;
                },
                strLitExpr -> {
                    sb.appendln(var.getName(), " = \"", escapeJava(strLitExpr.getText()), "\";");
                    return null;
                },
                nullExpr -> {
                    sb.appendln(var.getName(), " = null;");
                    return null;
                },
                invokeExpr -> {
                    invokeExpr.getExpr(sb, type, var.getName());
                    return null;
                },
                invokeStaticExpr -> {
                    invokeStaticExpr.getExpr(sb, type, var.getName());
                    return null;
                },
                fieldStaticExpr -> {
                    fieldStaticExpr.getExpr(sb, type, var.getName());
                    return null;
                },
                intLitExpr -> {
                    sb.appendln(var.getName(), " = ", intLitExpr.getValue(), ";");
                    return null;
                },
                longLitExpr -> {
                    sb.appendln(var.getName(), " = ", longLitExpr.getValue(), "L;");
                    return null;
                },
                floatLitExpr -> {
                    sb.appendln(var.getName(), " = ", floatLitExpr.getValue(), "f;");
                    return null;
                },
                doubleLitExpr -> {
                    sb.appendln(var.getName(), " = ", doubleLitExpr.getValue(), "d;");
                    return null;
                },
                booleanLitExpr -> {
                    sb.appendln(var.getName(), " = ", booleanLitExpr.getValue(), ";");
                    return null;
                },
                charLitExpr -> {
                    // We use escapeEcmaScript instead of escapeJava for single quoted to be escaped
                    sb.appendln(var.getName(), " = '", escapeEcmaScript(String.valueOf(charLitExpr.getValue())), "';");
                    return null;
                },
                classLitExpr -> {
                    sb.appendln(var.getName(), " = ", classLitExpr.getType().getText(), ".class;");
                    return null;
                },
                equalOpExpr -> {
                    sb.appendln(var.getName(), " = ", equalOpExpr.getLhs().getName(), " == ", equalOpExpr.getRhs().getName(), ";");
                    return null;
                },
                arrayExpr -> {
                    arrayExpr.getExpr(sb, type, var.getName());
                    return null;
                });
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarInitStatement(this);
    }
}
