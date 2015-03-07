package yokohama.unit.translator;

import java.util.List;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.util.GenSym;

public interface ExpressionStrategy {
    List<Statement> env(String varName); // introduce new environment
    List<Statement> bind(String envVarName, String name, Var rhs, GenSym genSym); // make a binding in the environment
    CatchClause catchAndAssignCause(String caughtVarName, String causeVarName, GenSym genSym);
}
