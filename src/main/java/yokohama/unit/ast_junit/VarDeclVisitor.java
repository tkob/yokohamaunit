package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.Pair;

public class VarDeclVisitor {
    public static List<Pair<Type, String>> sortedSet(Stream<Pair<Type, String>> i) {
        return i.collect(Collectors.toSet())
                .stream()
                .sorted((o1, o2) -> o1.getSecond().compareTo(o2.getSecond()))
                .collect(Collectors.toList());
    }

    public Stream<Pair<Type, String>> visitTestMethod(TestMethod testMethod) {
        return Stream.concat(
                visitStatements(testMethod.getBefore()),
                Stream.concat(
                        visitStatements(testMethod.getStatements()),
                        visitStatements(testMethod.getActionsAfter())));
    }

    public Stream<Pair<Type, String>> visitStatements(List<Statement> statements) {
        return statements.stream().flatMap(this::visitStatement);
    }

    public Stream<Pair<Type, String>> visitStatement(Statement statement) { 
        return statement.accept(
                isStatement -> Stream.<Pair<Type, String>>empty(),
                isNotStatement -> Stream.<Pair<Type, String>>empty(),
                varInitStatement ->
                        Stream.of(new Pair<Type, String>(varInitStatement.getType(), varInitStatement.getName())),
                returnIsStatement -> Stream.<Pair<Type, String>>empty(),
                returnIsNotStatement -> Stream.<Pair<Type, String>>empty(),
                invokeVoidStatement -> Stream.<Pair<Type, String>>empty(),
                tryStatement ->
                        Stream.concat(
                                visitStatements(tryStatement.getTryStatements()),
                                Stream.concat(
                                        tryStatement.getCatchClauses().stream().flatMap(this::visitCatchClause),
                                        visitStatements(tryStatement.getFinallyStatements()))));
    }

    public Stream<Pair<Type, String>> visitCatchClause(CatchClause catchClause) { 
        return visitStatements(catchClause.getStatements());
    }
}