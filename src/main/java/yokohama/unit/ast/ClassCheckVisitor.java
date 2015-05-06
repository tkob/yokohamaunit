package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.Either;
import yokohama.unit.util.Optionals;
import yokohama.unit.util.Pair;

@AllArgsConstructor
public class ClassCheckVisitor {
    private final ClassLoader classLoader;

    public Either<List<ErrorMessage>, ClassResolver> check(Group group) {
        Pair<ClassResolver, Stream<ErrorMessage>> classResolverAndErrors=
                visitAbbreviations(group.getAbbreviations());
        ClassResolver classResolver = classResolverAndErrors.getFirst();
        Stream<ErrorMessage> abbreviationErrors = classResolverAndErrors.getSecond();

        Stream<ErrorMessage> definitionErrors =
                new ClassExprCheckVisitor(group, classResolver).check();

        List<ErrorMessage> errors =
                Stream.concat(abbreviationErrors, definitionErrors)
                        .collect(Collectors.toList());
        return errors.size() > 0
                ? Either.left(errors)
                : Either.right(classResolver);
    }

    private Pair<ClassResolver, Stream<ErrorMessage>> visitAbbreviations(
            List<Abbreviation> abbreviations) {
        List<Either<ErrorMessage, Pair<String, String>>> bindingsOrErrors =
                abbreviations.stream().map(abbreviation -> {
                    String longName = abbreviation.getLongName();
                    Span span = abbreviation.getSpan();
                    try {
                        Class.forName(longName, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        return Either.<ErrorMessage, Pair<String, String>>left(
                                new ErrorMessage("cannot find class: " + longName, span));
                    }
                    return Either.<ErrorMessage, Pair<String, String>>right(abbreviation.toPair());
                }).collect(Collectors.toList());
        Stream<Pair<String, String>> bindings =
                bindingsOrErrors.stream().flatMap(Either::rightStream);
        Stream<ErrorMessage> errors =
                bindingsOrErrors.stream().flatMap(Either::leftStream);
        return new Pair<>(new ClassResolver(bindings, classLoader), errors);
    }
}

@AllArgsConstructor
class ClassExprCheckVisitor {
    private final Group group;
    private final ClassResolver classResolver;

    public Stream<ErrorMessage> check() {
        return group.getDefinitions().stream().flatMap(this::visitDefinition);
    }

    private Stream<ErrorMessage> visitDefinition(Definition definition) {
        return definition.accept(
                this::visitTest,
                this::visitFourPhaseTest,
                this::visitTable);
    }

    private Stream<ErrorMessage> visitTest(Test test) {
        return test.getAssertions().stream()
                .flatMap(assertion -> visitAssertion(assertion));
    }

    private Stream<ErrorMessage> visitAssertion(Assertion assertion) {
        Stream<ErrorMessage> propositionErrors =
                assertion.getPropositions().stream()
                        .flatMap(this::visitProposition);
        Stream<ErrorMessage> fixtureErrors = visitFixture(assertion.getFixture());
        return Stream.concat(propositionErrors, fixtureErrors);
    }

    private Stream<ErrorMessage> visitProposition(Proposition proposition) {
        return visitPredicate(proposition.getPredicate());
    }

    private Stream<ErrorMessage> visitPredicate(Predicate predicate) {
        return predicate.accept(
                isPredicate -> visitMatcher(isPredicate.getComplement()),
                isNotPredicate -> visitMatcher(isNotPredicate.getComplement()),
                throwsPredicate -> visitMatcher(throwsPredicate.getThrowee()));
    }

    private Stream<ErrorMessage> visitMatcher(Matcher matcher) {
        return matcher.accept(
            equalTo -> Stream.<ErrorMessage>empty(),
            instanceOf -> visitClassType(instanceOf.getClazz()),
            instanceSuchThat ->
                    Stream.concat(
                            visitClassType(instanceSuchThat.getClazz()),
                            instanceSuchThat.getPropositions().stream()
                                    .flatMap(this::visitProposition)),
            nullValue -> Stream.<ErrorMessage>empty());
    }

    private Stream<ErrorMessage> visitClassType(ClassType classType) {
        String name = classType.getName();
        try {
            classResolver.lookup(name);
        } catch (ClassNotFoundException e) {
            return Stream.of(new ErrorMessage(
                    "cannot resolve class: " + name,
                    classType.getSpan()));
        }
        return Stream.empty();
    }

    private Stream<ErrorMessage> visitFixture(Fixture fixture) {
        return fixture.accept(
                () -> Stream.<ErrorMessage>empty(),
                tableRef -> Stream.<ErrorMessage>empty(),
                bindings ->
                        bindings.getBindings().stream()
                                .flatMap(this::visitBinding));
    }

    private Stream<ErrorMessage> visitBinding(Binding binding) {
        return visitExpr(binding.getValue());
    }

    private Stream<ErrorMessage> visitExpr(Expr expr) {
        return expr.accept(
                quotedExpr -> Stream.<ErrorMessage>empty(),
                stubExpr -> visitStubExpr(stubExpr),
                invocationExpr -> visitInvocationExpr(invocationExpr),
                integerExpr -> Stream.<ErrorMessage>empty(),
                floatingPointExpr -> Stream.<ErrorMessage>empty(),
                booleanExpr -> Stream.<ErrorMessage>empty(),
                charExpr -> Stream.<ErrorMessage>empty(),
                stringExpr -> Stream.<ErrorMessage>empty());
    }

    private Stream<ErrorMessage> visitStubExpr(StubExpr stubExpr) {
        return Stream.concat(
                visitClassType(stubExpr.getClassToStub()),
                stubExpr.getBehavior().stream().flatMap(this::visitBehavior));
    }

    private Stream<ErrorMessage> visitInvocationExpr(InvocationExpr invocationExpr) {
        return Stream.concat(
                visitClassType(invocationExpr.getClassType()),
                Stream.concat(
                        visitMethodPattern(invocationExpr.getMethodPattern()),
                        invocationExpr.getArgs().stream().flatMap(this::visitExpr)));
    }

    private Stream<ErrorMessage> visitBehavior(StubBehavior behavior) {
        return Stream.concat(
                visitMethodPattern(behavior.getMethodPattern()),
                visitExpr(behavior.getToBeReturned()));
    }

    private Stream<ErrorMessage> visitMethodPattern(MethodPattern methodPattern) {
        return methodPattern.getParamTypes().stream().flatMap(this::visitType);
    }

    private Stream<ErrorMessage> visitType(Type type) {
        return type.getNonArrayType().accept(
                primitiveType -> Stream.<ErrorMessage>empty(),
                classType -> visitClassType(classType));
    }

    private Stream<ErrorMessage> visitFourPhaseTest(FourPhaseTest fourPhaseTest) {
        Stream<ErrorMessage> setup
                = Optionals.toStream(fourPhaseTest.getSetup()).flatMap(this::visitPhase);
        Stream<ErrorMessage> exercise =
                Optionals.toStream(fourPhaseTest.getExercise()).flatMap(this::visitPhase);
        Stream<ErrorMessage> verify =
                visitVerifyPhase(fourPhaseTest.getVerify());
        Stream<ErrorMessage> teardown =
                Optionals.toStream(fourPhaseTest.getTeardown()).flatMap(this::visitPhase);
        return Stream.concat(setup,
                Stream.concat(exercise,
                        Stream.concat(verify, teardown)));
    }

    private Stream<ErrorMessage> visitPhase(Phase phase) {
        return Stream.concat(
                phase.getLetStatements().stream()
                        .map(LetStatement::getBindings).flatMap(List::stream)
                        .flatMap(this::visitLetBinding),
                phase.getExecutions().stream()
                        .map(Execution::getExpressions).flatMap(List::stream)
                        .flatMap(this::visitExpr));
    }

    private Stream<ErrorMessage> visitLetBinding(LetBinding letBinding) {
        return visitExpr(letBinding.getValue());
    }

    private Stream<ErrorMessage> visitVerifyPhase(VerifyPhase verifyPhase) {
        return verifyPhase.getAssertions().stream().flatMap(this::visitAssertion);
    }

    private Stream<ErrorMessage> visitTable(Table table) {
        return table.getRows().stream().flatMap(this::visitRow);
    }

    private Stream<ErrorMessage> visitRow(Row row) {
        return row.getExprs().stream().flatMap(this::visitExpr);
    }
}
