package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.util.GenSym;

public interface ExpressionStrategy {
    List<Statement> env(String varName, GenSym genSym); // introduce new environment
    List<Statement> bind(String envVarName, String name, Var rhs, GenSym genSym); // make a binding in the environment
    CatchClause catchAndAssignCause(String causeVarName, GenSym genSym);
    List<Statement> eval(String varName, String envVarName, QuotedExpr quotedExpr, GenSym genSym, Optional<Path> docyPath, String className, String packageName);
}
