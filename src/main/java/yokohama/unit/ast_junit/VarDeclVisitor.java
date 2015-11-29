package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaslang.Tuple;
import javaslang.Tuple2;

public class VarDeclVisitor {
    public static List<Tuple2<Type, String>> sortedSet(Stream<Tuple2<Type, String>> i) {
        return i.collect(Collectors.toSet())
                .stream()
                .sorted((o1, o2) -> o1._2().compareTo(o2._2()))
                .collect(Collectors.toList());
    }

    public Stream<Tuple2<Type, String>> visitMethod(Method method) {
        return visitStatements(method.getStatements());
    }

    public Stream<Tuple2<Type, String>> visitStatements(List<Statement> statements) {
        return statements.stream().flatMap(this::visitStatement);
    }

    public Stream<Tuple2<Type, String>> visitStatement(Statement statement) { 
        return statement.accept(
                isStatement -> Stream.<Tuple2<Type, String>>empty(),
                varInitStatement ->
                        Stream.of(Tuple.of(varInitStatement.getType(), varInitStatement.getVar().getName())),
                tryStatement ->
                        Stream.concat(
                                visitStatements(tryStatement.getTryStatements()),
                                Stream.concat(
                                        tryStatement.getCatchClauses().stream().flatMap(this::visitCatchClause),
                                        visitStatements(tryStatement.getFinallyStatements()))),
                throwStatement -> Stream.<Tuple2<Type, String>>empty(),
                ifStatement ->
                        Stream.concat(
                                visitStatements(ifStatement.getThen()),
                                visitStatements(ifStatement.getOtherwise())),
                returnStatement -> Stream.<Tuple2<Type, String>>empty(),
                invokeVoidStatement -> Stream.<Tuple2<Type, String>>empty(),
                invokeStaticVoidStatement -> Stream.<Tuple2<Type, String>>empty());
    }

    public Stream<Tuple2<Type, String>> visitCatchClause(CatchClause catchClause) { 
        return visitStatements(catchClause.getStatements());
    }
}
