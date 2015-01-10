package yokohama.unit.ast_junit;

import java.util.Set;
import java.util.function.Function;
import yokohama.unit.util.SBuilder;

public interface Statement {
    void toString(SBuilder sb, ExpressionStrategy expressionStrategy);

    Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy);

    <T> T accept(TestStatementVisitor<T> visitor);

    default <T> T accept(
            Function<IsStatement, T> visitIsStatement_,
            Function<IsNotStatement, T> visitIsNotStatement_,
            Function<ThrowsStatement, T> visitThrowsStatement_
    ) {
        return accept(new TestStatementVisitor<T>() {
            @Override
            public T visitIsStatement(IsStatement isStatement) {
                return visitIsStatement_.apply(isStatement);
            }

            @Override
            public T visitIsNotStatement(IsNotStatement isNotStatement) {
                return visitIsNotStatement_.apply(isNotStatement);
            }

            @Override
            public T visitThrowsStatement(ThrowsStatement throwsStatement) {
                return visitThrowsStatement_.apply(throwsStatement);
            }
        });
    }
}
