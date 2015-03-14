package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.Pair;

public class VarDeclVisitor {
    public List<Pair<Type, String>> visitTestMethod(TestMethod testMethod) {
        return Stream.concat(
                testMethod.getBefore().stream().flatMap(this::visitStatement),
                testMethod.getStatements().stream().flatMap(this::visitStatement))
                .collect(Collectors.toSet())
                .stream()
                .sorted((o1, o2) -> o1.getSecond().compareTo(o2.getSecond()))
                .collect(Collectors.toList());
    }

    public Stream<Pair<Type, String>> visitStatement(Statement statement) { 
        return statement.accept(
                isStatement -> Stream.<Pair<Type, String>>empty(),
                isNotStatement -> Stream.<Pair<Type, String>>empty(),
                actionStatement -> Stream.<Pair<Type, String>>empty(),
                varInitStatement ->
                        Stream.of(new Pair<Type, String>(varInitStatement.getType(), varInitStatement.getName())),
                returnIsStatement -> Stream.<Pair<Type, String>>empty(),
                returnIsNotStatement -> Stream.<Pair<Type, String>>empty(),
                invokeVoidStatement -> Stream.<Pair<Type, String>>empty(),
                tryStatement ->
                        Stream.concat(
                                tryStatement.getTryStatements().stream().flatMap(this::visitStatement),
                                Stream.concat(
                                        tryStatement.getCatchClauses().stream().flatMap(this::visitCatchClause),
                                        tryStatement.getFinallyStatements().stream().flatMap(this::visitStatement))));
    }

    public Stream<Pair<Type, String>> visitCatchClause(CatchClause catchClause) { 
        return catchClause.getStatements().stream().flatMap(this::visitStatement);
    }
}
