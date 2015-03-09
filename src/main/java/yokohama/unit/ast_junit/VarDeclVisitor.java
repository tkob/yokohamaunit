package yokohama.unit.ast_junit;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.Pair;

public class VarDeclVisitor {
    public Set<Pair<ClassType, String>> visitTestMethod(TestMethod testMethod) {
        TreeSet<Pair<ClassType, String>> varDecls = new TreeSet<>();
        varDecls.addAll(Stream.concat(
                testMethod.getBefore().stream().flatMap(this::visitStatement),
                testMethod.getStatements().stream().flatMap(this::visitStatement))
                .collect(Collectors.toSet()));
        return varDecls;

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
                                tryStatement.getFinallyStatements().stream().flatMap(this::visitStatement)),
                varDeclStatement ->
                        Stream.of(new Pair<ClassType, String>(varDeclStatement.getClassType(), varDeclStatement.getName())),
                varAssignStatement -> Stream.<Pair<ClassType, String>>empty());
    }
}
