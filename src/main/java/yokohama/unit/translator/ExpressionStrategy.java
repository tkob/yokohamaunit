package yokohama.unit.translator;

import java.util.Collection;
import java.util.List;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.util.ClassResolver;

public interface ExpressionStrategy {
    Collection<ClassDecl> auxClasses(ClassResolver classResolver);

    List<Statement> env( String varName, ClassResolver classResolver); // introduce new environment

    List<Statement> bind(String envVarName, String name, Var rhs); // make a binding in the environment

    CatchClause catchAndAssignCause(String causeVarName);

    List<Statement> eval(String varName, String envVarName, QuotedExpr quotedExpr);
}
