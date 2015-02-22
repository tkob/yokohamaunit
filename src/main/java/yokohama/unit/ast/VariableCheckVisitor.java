package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.FList;
import yokohama.unit.util.Optionals;
import static yokohama.unit.util.Optionals.match;
import yokohama.unit.util.Pair;

public class VariableCheckVisitor {
    private final Group group;
    private final List<Table> tables;
    private final TableExtractVisitor tableExtractVisitor = new TableExtractVisitor();

    public VariableCheckVisitor(Group group) {
        this.group = group;
        this.tables = tableExtractVisitor.extractTables(group);
    }

    static ErrorMessage mkErr(String var, Span span) {
        return new ErrorMessage("variable " + var + " is already defined", span);
    }

    public List<ErrorMessage> check() {
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
                table -> checkTable(table, env));
    }

    private Stream<ErrorMessage> checkTest(Test test, FList<String> env) {
        return test.getAssertions()
                .stream()
                .flatMap(assertion -> checkAssertion(assertion, env));
    }

    private Stream<ErrorMessage> checkAssertion(Assertion assertion, FList<String> env) {
        return Stream.concat(
                assertion.getPropositions().stream().flatMap(proposition -> checkProposition(proposition, env)),
                checkFixture(assertion.getFixture(), env));
    }

    private Stream<ErrorMessage> checkProposition(Proposition proposition, FList<String> env) {
        return checkPredicate(proposition.getPredicate(), env);
    }

    private Stream<ErrorMessage> checkPredicate(Predicate predicate, FList<String> env) {
        Matcher matcher = predicate.accept(
                IsPredicate::getComplement, 
                IsNotPredicate::getComplement, 
                ThrowsPredicate::getThrowee); 
        return checkMatcher(matcher, env);
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

    private Stream<ErrorMessage> checkFixture(Fixture fixture, FList<String> env) {
        return fixture.<Stream<ErrorMessage>>accept(
                () -> Stream.empty(),
                tableRef -> checkIdents(tableRef.getIdents(), env),
                bindings -> checkBindings(bindings, env));
    }

    private Stream<ErrorMessage> checkIdents(List<Ident> idents, FList<String> env) {
        return FList.fromList(idents).foldLeft(
                new Pair<FList<String>, FList<ErrorMessage>>(env, FList.empty()), (pair, ident) -> {
                    String var = ident.getName();
                    Span span = ident.getSpan();
                    FList<String> env_ = pair.getFirst();
                    FList<ErrorMessage> msgs = pair.getSecond();
                    return new Pair<>(
                            env_.add(var),
                            env_.contains(var) ? msgs.add(mkErr(var, span))
                                               : msgs);
                })
                .getSecond().toReverseList().stream();
    }

    private Stream<ErrorMessage> checkBindings(Bindings bindings, FList<String> env) {
        List<Ident> idents = bindings.getBindings()
                .stream().map(Binding::getName).collect(Collectors.toList());
        return checkIdents(idents, env);
    }

    private Stream<ErrorMessage> checkFourPhaseTest(FourPhaseTest fourPhaseTest, FList<String> env) {
        List<Ident> setupIdents = Optionals.<Phase, Stream<Ident>>match(fourPhaseTest.getSetup(),
                () -> Stream.empty(),
                phase -> match(phase.getLetBindings(),
                        () -> Stream.empty(),
                        letBindings ->
                                letBindings.getBindings().stream().map(
                                        letBinding ->
                                                new Ident(letBinding.getName(), letBinding.getSpan()))))
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
