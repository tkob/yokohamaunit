package yokohama.unit.translator;

import java.util.Collection;
import java.util.List;
import yokohama.unit.ast.StubExpr;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.Sym;

public interface MockStrategy {
    /**
     * Supplies auxiliary classes.
     * 
     * @return a collection of auxiliary classes
     */
    Collection<ClassDecl> auxClasses();

    /**
     * Create a stub.
     * 
     * @param var                a variable to be bound to the stub
     * @param stubExpr           a stub expression
     * @param astToJUnitAstVisitor 
     * @param envVar             a variable bound to the environment
     * @return statements that bind the variable name to the stub
     */
    List<Statement> stub(
            Sym var,
            StubExpr stubExpr,
            AstToJUnitAstVisitor astToJUnitAstVisitor,
            Sym envVar);
}
