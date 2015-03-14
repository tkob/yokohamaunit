package yokohama.unit.ast_junit;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MockUsedVisitor {
    private final ClassDecl classDecl;
    
    public boolean mockUsed() {
        return visitClassDecl(classDecl);
    }

    private boolean visitClassDecl(ClassDecl classDecl) {
        return classDecl.getTestMethods().stream().anyMatch(this::visitTestMethod);
    }

    private boolean visitTestMethod(TestMethod testMethod) {
        return testMethod.getStatements().stream().anyMatch(this::visitStatement);
    }

    private boolean visitStatement(Statement statement) {
        return statement.accept(
                isStatement -> false,
                isNotStatement -> false,
                varDeclStatement -> visitExpr(varDeclStatement.getValue()),
                returnIsStatement -> false,
                returnIsNotStatement -> false,
                invokeVoidStatement -> false,
                tryStatement ->
                        tryStatement.getTryStatements().stream().anyMatch(this::visitStatement)
                     || tryStatement.getCatchClauses().stream().anyMatch(catchClause ->
                             catchClause.getStatements().stream().anyMatch(this::visitStatement))
                     || tryStatement.getFinallyStatements().stream().anyMatch(this::visitStatement));
    }

    private boolean visitExpr(Expr value) {
        return value.accept(
                var -> false,
                quoted -> false,
                stubExpr -> true,
                matcherExpr -> false,
                newExpr -> false,
                strLitExpr -> false,
                nullExpr -> false,
                invokeExpr -> false,
                thisExpr -> false,
                invokeStaticExpr -> false,
                intLitExpr -> false);
    }
}
