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
                actionStatement -> false,
                topBindStatement -> false,
                varDeclStatement -> visitExpr(varDeclStatement.getValue()),
                bindThrownStatement -> visitExpr(bindThrownStatement.getValue()),
                returnIsStatement -> false,
                returnIsNotStatement -> false);
    }

    private boolean visitExpr(Expr value) {
        return value.accept(
                quoted -> false,
                stubExpr -> true,
                matcherExpr -> false);
    }
}
