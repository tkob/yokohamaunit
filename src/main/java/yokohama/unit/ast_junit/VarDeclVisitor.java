package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.Pair;

public class VarDeclVisitor {
    public List<Pair<ClassType, String>> visitTestMethod(TestMethod testMethod) {
        return Stream.concat(
                testMethod.getBefore().stream().flatMap(this::visitStatement),
                testMethod.getStatements().stream().flatMap(this::visitStatement))
                .collect(Collectors.toSet())
                .stream()
                .sorted((o1, o2) -> o1.getSecond().compareTo(o2.getSecond()))
                .collect(Collectors.toList());
    }

    public Stream<Pair<ClassType, String>> visitStatement(Statement statement) { 
        return statement.accept(
                isStatement -> Stream.<Pair<ClassType, String>>empty(),
                isNotStatement -> Stream.<Pair<ClassType, String>>empty(),
                actionStatement -> Stream.<Pair<ClassType, String>>empty(),
                varInitStatement ->
                        Stream.of(new Pair<ClassType, String>(varInitStatement.getType(), varInitStatement.getName())),
                returnIsStatement -> Stream.<Pair<ClassType, String>>empty(),
                returnIsNotStatement -> Stream.<Pair<ClassType, String>>empty(),
                invokeVoidStatement -> Stream.<Pair<ClassType, String>>empty(),
                tryStatement ->
                        Stream.concat(
                                tryStatement.getTryStatements().stream().flatMap(this::visitStatement),
                                Stream.concat(
                                        tryStatement.getCatchClauses().stream().flatMap(this::visitCatchClause),
                                        tryStatement.getFinallyStatements().stream().flatMap(this::visitStatement))),
                varAssignStatement -> Stream.<Pair<ClassType, String>>empty());
    }

    public Stream<Pair<ClassType, String>> visitCatchClause(CatchClause catchClause) { 
        return catchClause.getStatements().stream().flatMap(this::visitStatement);
    }
}
