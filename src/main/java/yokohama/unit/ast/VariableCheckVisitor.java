package yokohama.unit.ast;

import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.FList;
import yokohama.unit.util.Optionals;
import yokohama.unit.util.Pair;

public class VariableCheckVisitor {
    static ErrorMessage mkErr(String var, Span span) {
        return new ErrorMessage("variable " + var + " is already defined", span);
    }

    public List<ErrorMessage> check(Group group) {
        FList<String> env = FList.empty();
        return checkGroup(group, env).collect(Collectors.toList());
    }

    private Stream<ErrorMessage> checkGroup(Group group, FList<String> env) {
        return group.getDefinitions()
                .stream()
                .flatMap(definition -> checkDefinition(definition, env));
    }

    private Stream<ErrorMessage> checkDefinition(Definition definition, FList<String> env) {
        return definition.accept(
                test -> checkTest(test, env),
                fourPhaseTest -> checkFourPhaseTest(fourPhaseTest, env),
                table -> checkTable(table, env),
                codeBlock -> Stream.<ErrorMessage>empty(),
                heading -> Stream.<ErrorMessage>empty());
    }

    private Stream<ErrorMessage> checkTest(Test test, FList<String> env) {
        return test.getAssertions()
                .stream()
                .flatMap(assertion -> checkAssertion(assertion, env));
    }

    private Stream<ErrorMessage> checkAssertion(Assertion assertion, FList<String> env) {
        return Stream.concat(
                assertion.getClauses().stream()
                        .flatMap(clause -> checkClause(clause, env)),
                checkFixture(assertion.getFixture(), env));
    }

    private Stream<ErrorMessage> checkClause(Clause clause, FList<String> env) {
        return clause.getPropositions().stream()
                .flatMap(proposition -> checkProposition(proposition, env));
    }

    private Stream<ErrorMessage> checkProposition(Proposition proposition, FList<String> env) {
        return checkPredicate(proposition.getPredicate(), env);
    }

    private Stream<ErrorMessage> checkPredicate(Predicate predicate, FList<String> env) {
        return predicate.accept(
                isPredicate -> checkMatcher(isPredicate.getComplement(), env),
                isNotPredicate -> checkMatcher(isNotPredicate.getComplement(), env),
                throwsPredicate -> checkMatcher(throwsPredicate.getThrowee(), env),
                matchesPredicate -> checkPattern(matchesPredicate.getPattern(), env),
                doesNotMatchPredicate -> checkPattern(doesNotMatchPredicate.getPattern(), env));
    }

    private Stream<ErrorMessage> checkMatcher(Matcher matcher, FList<String> env) {
        return matcher.<Stream<ErrorMessage>>accept(
                equalTo -> Stream.empty(),
                instanceOf -> Stream.empty(),
                instanceSuchThat -> {
                    Ident ident = instanceSuchThat.getVar();
                    String var = ident.getName();
                    Stream<ErrorMessage> err =
                            env.contains(var) ? Stream.of(mkErr(var, ident.getSpan()))
                                              : Stream.empty();
                    FList<String> newEnv = env.add(var);
                    Stream<ErrorMessage> errs =
                            instanceSuchThat.getPropositions()
                                    .stream()
                                    .flatMap(proposition -> checkProposition(proposition, newEnv));
                    return Stream.concat(err, errs);
                },
                nullValue -> Stream.empty());
    }

    private Stream<ErrorMessage> checkPattern(Pattern pattern, FList<String> env) {
        return Stream.empty();
    }

    private Stream<ErrorMessage> checkFixture(Fixture fixture, FList<String> env) {
        return fixture.<Stream<ErrorMessage>>accept(
                () -> Stream.empty(),
                tableRef -> checkIdents(tableRef.getIdents(), env),
                bindings -> checkBindings(bindings, env));
    }

    private Stream<ErrorMessage> checkIdents(List<Ident> idents, FList<String> env) {
        return FList.fromList(idents).foldLeft(
                Pair.<FList<String>, FList<ErrorMessage>>of(env, FList.empty()), (pair, ident) -> {
                    String var = ident.getName();
                    Span span = ident.getSpan();
                    FList<String> env_ = pair.getFirst();
                    FList<ErrorMessage> msgs = pair.getSecond();
                    return Pair.of(
                            env_.add(var),
                            env_.contains(var) ? msgs.add(mkErr(var, span))
                                               : msgs);
                })
                .getSecond().toReverseList().stream();
    }

    private Stream<ErrorMessage> checkBindings(Bindings bindings, FList<String> env) {
        List<Ident> idents = bindings.getBindings().stream()
                .flatMap(binding -> binding.accept(
                        single -> Stream.of(single.getName()),
                        choice -> Stream.of(choice.getName()),
                        table -> table.getIdents().stream()))
                .collect(Collectors.toList());
        return checkIdents(idents, env);
    }

    private Stream<ErrorMessage> checkFourPhaseTest(FourPhaseTest fourPhaseTest, FList<String> env) {
        List<Ident> setupIdents = Optionals.<Phase, Stream<Ident>>match(fourPhaseTest.getSetup(),
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

        FList<String> newEnv = FList.fromList(setupIdents)
                .foldLeft(env, (env_, ident) -> env_.add(ident.getName()));
        Stream<ErrorMessage> verifyErrors = checkVerifyPhase(fourPhaseTest.getVerify(), newEnv);

        return Stream.concat(setupErrors, verifyErrors);
    }

    private Stream<ErrorMessage> checkVerifyPhase(VerifyPhase verify, FList<String> env) {
        return verify.getAssertions()
                .stream()
                .flatMap(assertion -> checkAssertion(assertion, env));
    }

    private Stream<ErrorMessage> checkTable(Table table, FList<String> env) {
        return checkIdents(table.getHeader(), env);
    }
}
