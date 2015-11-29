package yokohama.unit.ast;

import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaslang.Tuple;
import javaslang.collection.List;
import yokohama.unit.util.Optionals;

public class VariableCheckVisitor {
    static ErrorMessage mkErr(String var, Span span) {
        return new ErrorMessage("variable " + var + " is already defined", span);
    }

    public java.util.List<ErrorMessage> check(Group group) {
        List<String> env = List.empty();
        return checkGroup(group, env).collect(Collectors.toList());
    }

    private Stream<ErrorMessage> checkGroup(Group group, List<String> env) {
        return group.getDefinitions()
                .stream()
                .flatMap(definition -> checkDefinition(definition, env));
    }

    private Stream<ErrorMessage> checkDefinition(Definition definition, List<String> env) {
        return definition.accept(
                test -> checkTest(test, env),
                fourPhaseTest -> checkFourPhaseTest(fourPhaseTest, env),
                table -> checkTable(table, env),
                codeBlock -> Stream.<ErrorMessage>empty(),
                heading -> Stream.<ErrorMessage>empty());
    }

    private Stream<ErrorMessage> checkTest(Test test, List<String> env) {
        return test.getAssertions()
                .stream()
                .flatMap(assertion -> checkAssertion(assertion, env));
    }

    private Stream<ErrorMessage> checkAssertion(Assertion assertion, List<String> env) {
        return Stream.concat(
                assertion.getClauses().stream()
                        .flatMap(clause -> checkClause(clause, env)),
                checkFixture(assertion.getFixture(), env));
    }

    private Stream<ErrorMessage> checkClause(Clause clause, List<String> env) {
        return clause.getPropositions().stream()
                .flatMap(proposition -> checkProposition(proposition, env));
    }

    private Stream<ErrorMessage> checkProposition(Proposition proposition, List<String> env) {
        return checkPredicate(proposition.getPredicate(), env);
    }

    private Stream<ErrorMessage> checkPredicate(Predicate predicate, List<String> env) {
        return predicate.accept(
                isPredicate -> checkMatcher(isPredicate.getComplement(), env),
                isNotPredicate -> checkMatcher(isNotPredicate.getComplement(), env),
                throwsPredicate -> checkMatcher(throwsPredicate.getThrowee(), env),
                matchesPredicate -> checkPattern(matchesPredicate.getPattern(), env),
                doesNotMatchPredicate -> checkPattern(doesNotMatchPredicate.getPattern(), env));
    }

    private Stream<ErrorMessage> checkMatcher(Matcher matcher, List<String> env) {
        return matcher.<Stream<ErrorMessage>>accept(
                equalTo -> Stream.empty(),
                instanceOf -> Stream.empty(),
                instanceSuchThat -> {
                    Ident ident = instanceSuchThat.getVar();
                    String var = ident.getName();
                    Stream<ErrorMessage> err =
                            env.contains(var) ? Stream.of(mkErr(var, ident.getSpan()))
                                              : Stream.empty();
                    List<String> newEnv = env.push(var);
                    Stream<ErrorMessage> errs =
                            instanceSuchThat.getPropositions()
                                    .stream()
                                    .flatMap(proposition -> checkProposition(proposition, newEnv));
                    return Stream.concat(err, errs);
                },
                nullValue -> Stream.empty());
    }

    private Stream<ErrorMessage> checkPattern(Pattern pattern, List<String> env) {
        return Stream.empty();
    }

    private Stream<ErrorMessage> checkFixture(Fixture fixture, List<String> env) {
        return fixture.<Stream<ErrorMessage>>accept(
                () -> Stream.empty(),
                tableRef -> checkIdents(tableRef.getIdents(), env),
                bindings -> checkBindings(bindings, env));
    }

    private Stream<ErrorMessage> checkIdents(
            java.util.List<Ident> idents, List<String> env) {
        return List.ofAll(idents).foldLeft(
                Tuple.<List<String>, List<ErrorMessage>>of(env, List.empty()), (pair, ident) -> {
                    String var = ident.getName();
                    Span span = ident.getSpan();
                    List<String> env_ = pair._1();
                    List<ErrorMessage> msgs = pair._2();
                    return Tuple.of(
                            env_.push(var),
                            env_.contains(var) ? msgs.push(mkErr(var, span))
                                               : msgs);
                })
                ._2().reverse().toJavaStream();
    }

    private Stream<ErrorMessage> checkBindings(Bindings bindings, List<String> env) {
        java.util.List<Ident> idents = bindings.getBindings().stream()
                .flatMap(binding -> binding.accept(
                        single -> Stream.of(single.getName()),
                        choice -> Stream.of(choice.getName()),
                        table -> table.getIdents().stream()))
                .collect(Collectors.toList());
        return checkIdents(idents, env);
    }

    private Stream<ErrorMessage> checkFourPhaseTest(FourPhaseTest fourPhaseTest, List<String> env) {
        java.util.List<Ident> setupIdents =
                Optionals.<Phase, Stream<Ident>>match(fourPhaseTest.getSetup(),
                        () -> Stream.empty(),
                        phase -> phase.getLetStatements().stream()
                                .flatMap(letStatement ->
                                        letStatement.getBindings().stream()
                                                .flatMap(binding -> binding.accept(
                                                        single -> Stream.of(single.getName()),
                                                        choice -> Stream.of(choice.getName()),
                                                        table -> table.getIdents().stream()))))
                        .collect(Collectors.toList());
        Stream<ErrorMessage> setupErrors = checkIdents(setupIdents, env);

        List<String> newEnv = List.ofAll(setupIdents)
                .foldLeft(env, (env_, ident) -> env_.push(ident.getName()));
        Stream<ErrorMessage> verifyErrors = checkVerifyPhase(fourPhaseTest.getVerify(), newEnv);

        return Stream.concat(setupErrors, verifyErrors);
    }

    private Stream<ErrorMessage> checkVerifyPhase(VerifyPhase verify, List<String> env) {
        return verify.getAssertions()
                .stream()
                .flatMap(assertion -> checkAssertion(assertion, env));
    }

    private Stream<ErrorMessage> checkTable(Table table, List<String> env) {
        return checkIdents(table.getHeader(), env);
    }
}
