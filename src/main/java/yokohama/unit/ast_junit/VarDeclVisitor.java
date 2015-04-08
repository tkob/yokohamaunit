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

    public Stream<Pair<Type, String>> visitMethod(Method method) {
        return visitStatements(method.getStatements());
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
                tryStatement ->
                        Stream.concat(
                                visitStatements(tryStatement.getTryStatements()),
                                Stream.concat(
                                        tryStatement.getCatchClauses().stream().flatMap(this::visitCatchClause),
                                        visitStatements(tryStatement.getFinallyStatements()))),
                ifStatement ->
                        Stream.concat(
                                visitStatements(ifStatement.getThen()),
                                visitStatements(ifStatement.getOtherwise()))
                );
    }

    public Stream<Pair<Type, String>> visitCatchClause(CatchClause catchClause) { 
        return visitStatements(catchClause.getStatements());
    }
}
