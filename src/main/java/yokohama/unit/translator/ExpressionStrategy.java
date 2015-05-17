package yokohama.unit.translator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Var;

public interface ExpressionStrategy {
    /**
     * Supplies auxiliary classes.
     * 
     * @return a collection of auxiliary classes
     */
    Collection<ClassDecl> auxClasses();

    /**
     * Introduces a new environment.
     * 
     * @param varName       a variable name to be bound to the new environment
     * @return statements that introduce a new environment
     */
    List<Statement> env(String varName);

    /**
     * Bind a variable in the expression language to a value
     * 
     * @param envVarName a variable name (in Java) bound to the environment
     * @param ident      an identifier in the expression language to be bound
     *                    to ths
     * @param rhs        a variable (in Java) bound to the value to which the
     *                    name is to be bound
     * @return statements that bind name to rhs in the environment of the
     *          expression language
     */
    List<Statement> bind(String envVarName, Ident ident, Var rhs);

    Optional<CatchClause> catchAndAssignCause(String causeVarName);

    /**
     * Evaluates an expression.
     * 
     * @param varName      a variable name to be bound to the result
     * @param quotedExpr   an expression to be evaluated
     * @param expectedType Specifies the type that the resultant evaluation
     *                      will be coerced to.
     * @param envVarName   a variable name bound to the environment
     * @return statements that evaluates the expression
     */
    List<Statement> eval(
            String varName,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            String envVarName);
}
