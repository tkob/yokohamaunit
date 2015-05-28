package yokohama.unit.translator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.Sym;

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
     * @param var       a variable to be bound to the new environment
     * @return statements that introduce a new environment
     */
    List<Statement> env(Sym var);

    /**
     * Bind a variable in the expression language to a value
     * 
     * @param envVar     a variable (in Java) bound to the environment
     * @param ident      an identifier in the expression language to be bound
     *                    to ths
     * @param rhs        a variable (in Java) bound to the value to which the
     *                    name is to be bound
     * @return statements that bind name to rhs in the environment of the
     *          expression language
     */
    List<Statement> bind(Sym envVar, Ident ident, Sym rhs);

    Optional<CatchClause> catchAndAssignCause(Sym causeVar);

    /**
     * Evaluates an expression.
     * 
     * @param var          a variable to be bound to the result
     * @param quotedExpr   an expression to be evaluated
     * @param expectedType Specifies the type that the resultant evaluation
     *                      will be coerced to.
     * @param envVar       a variable bound to the environment
     * @return statements that evaluates the expression
     */
    List<Statement> eval(
            Sym var,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            Sym envVar);

    /**
     * Dump environment as a string
     * 
     * @param var    a variable bound to the dump string
     * @param envVar a variable bound to the environment
     * @return statements that dump the enviroment as a string
     */
    List<Statement> dumpEnv(Sym var, Sym envVar);
}
