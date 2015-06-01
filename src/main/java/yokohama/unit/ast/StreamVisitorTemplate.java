package yokohama.unit.ast;

import java.util.stream.Stream;
import yokohama.unit.util.Optionals;

public class StreamVisitorTemplate<T> extends AstVisitor<Stream<T>> {
    @Override
    public Stream<T> visitGroup(Group group) {
        return Stream.concat(
                group.getAbbreviations().stream().flatMap(this::visitAbbreviation),
                group.getDefinitions().stream().flatMap(this::visitDefinition));
    }

    @Override
    public Stream<T> visitAbbreviation(Abbreviation abbreviation) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitTest(Test test) {
        return test.getAssertions().stream().flatMap(this::visitAssertion);
    }

    @Override
    public Stream<T> visitAssertion(Assertion assertion) {
        return Stream.concat(
                assertion.getPropositions().stream().flatMap(this::visitProposition),
                visitFixture(assertion.getFixture()));
    }

    @Override
    public Stream<T> visitProposition(Proposition proposition) {
        return Stream.concat(
                visitExpr(proposition.getSubject()),
                visitPredicate(proposition.getPredicate()));
    }

    @Override
    public Stream<T> visitIsPredicate(IsPredicate isPredicate) {
        return visitMatcher(isPredicate.getComplement());
    }

    @Override
    public Stream<T> visitIsNotPredicate(IsNotPredicate isNotPredicate) {
        return visitMatcher(isNotPredicate.getComplement());
    }

    @Override
    public Stream<T> visitThrowsPredicate(ThrowsPredicate throwsPredicate) {
        return visitMatcher(throwsPredicate.getThrowee());
    }


    @Override
    public Stream<T> visitMatchesPredicate(MatchesPredicate matchesPredicate) {
        return visitPattern(matchesPredicate.getPattern());
    }

    @Override
    public Stream<T> visitDoesNotMatchPredicate(DoesNotMatchPredicate doesNotMatchPredicate) {
        return visitPattern(doesNotMatchPredicate.getPattern());
    }

    @Override
    public Stream<T> visitEqualToMatcher(EqualToMatcher equalTo) {
        return visitExpr(equalTo.getExpr());
    }

    @Override
    public Stream<T> visitInstanceOfMatcher(InstanceOfMatcher instanceOf) {
        return visitClassType(instanceOf.getClazz());
    }

    @Override
    public Stream<T> visitInstanceSuchThatMatcher(InstanceSuchThatMatcher instanceSuchThat) {
        return Stream.concat(
                Stream.concat(
                        visitClassType(instanceSuchThat.getClazz()),
                        visitIdent(instanceSuchThat.getVar())),
                instanceSuchThat.getPropositions().stream().flatMap(this::visitProposition));
    }

    @Override
    public Stream<T> visitNullValueMatcher(NullValueMatcher nullValue) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitRegExpPattern(RegExpPattern regExpPattern) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitQuotedExpr(QuotedExpr quotedExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitStubExpr(StubExpr stubExpr) {
        return Stream.concat(
                visitClassType(stubExpr.getClassToStub()),
                stubExpr.getBehavior().stream().flatMap(this::visitStubBehavior));
    }

    @Override
    public Stream<T> visitInvocationExpr(InvocationExpr invocationExpr) {
        return Stream.concat(
                Stream.concat(
                        visitClassType(invocationExpr.getClassType()),
                        visitMethodPattern(invocationExpr.getMethodPattern())),
                Stream.concat(
                        Optionals.toStream(invocationExpr.getReceiver()).flatMap(this::visitExpr),
                        invocationExpr.getArgs().stream().flatMap(this::visitExpr)));
    }

    @Override
    public Stream<T> visitIntegerExpr(IntegerExpr integerExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitFloatingPointExpr(FloatingPointExpr floatingPointExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitBooleanExpr(BooleanExpr booleanExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitCharExpr(CharExpr charExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitStringExpr(StringExpr stringExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitAnchorExpr(AnchorExpr anchorExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitAsExpr(AsExpr asExpr) {
        return Stream.concat(
                visitExpr(asExpr.getSourceExpr()),
                visitClassType(asExpr.getClassType()));
    }

    @Override
    public Stream<T> visitStubBehavior(StubBehavior behavior) {
        return Stream.concat(
                visitMethodPattern(behavior.getMethodPattern()),
                visitExpr(behavior.getToBeReturned()));
    }

    @Override
    public Stream<T> visitMethodPattern(MethodPattern methodPattern) {
        return methodPattern.getParamTypes().stream().flatMap(this::visitType);
    }

    @Override
    public Stream<T> visitType(Type type) {
        return visitNonArrayType(type.getNonArrayType());
    }

    @Override
    public Stream<T> visitPrimitiveType(PrimitiveType primitiveType) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitClassType(ClassType classType) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitNone() {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitTableRef(TableRef tableRef) {
        return tableRef.getIdents().stream().flatMap(this::visitIdent);
    }

    @Override
    public Stream<T> visitBindings(Bindings bindings) {
        return bindings.getBindings().stream().flatMap(this::visitBinding);
    }

    @Override
    public Stream<T> visitSingleBinding(SingleBinding singleBinding) {
        return Stream.concat(
                visitIdent(singleBinding.getName()),
                visitExpr(singleBinding.getValue()));
    }

    @Override
    public Stream<T> visitChoiceBinding(ChoiceBinding choiceBinding) {
        return Stream.concat(
                visitIdent(choiceBinding.getName()),
                choiceBinding.getChoices().stream().flatMap(this::visitExpr));
    }

    @Override
    public Stream<T> visitFourPhaseTest(FourPhaseTest fourPhaseTest) {
        return Stream.concat(
                Stream.concat(
                        Optionals.toStream(fourPhaseTest.getSetup()).flatMap(this::visitPhase),
                        Optionals.toStream(fourPhaseTest.getExercise()).flatMap(this::visitPhase)),
                Stream.concat(
                        visitVerifyPhase(fourPhaseTest.getVerify()),
                        Optionals.toStream(fourPhaseTest.getTeardown()).flatMap(this::visitPhase)));
    }

    @Override
    public Stream<T> visitPhase(Phase phase) {
        return Stream.concat(
                phase.getLetStatements().stream().flatMap(this::visitLetStatement),
                phase.getStatements().stream().flatMap(this::visitStatement));
    }

    @Override
    public Stream<T> visitVerifyPhase(VerifyPhase verifyPhase) {
        return verifyPhase.getAssertions().stream().flatMap(this::visitAssertion);
    }

    @Override
    public Stream<T> visitLetStatement(LetStatement letStatement) {
        return letStatement.getBindings().stream().flatMap(this::visitBinding);
    }

    @Override
    public Stream<T> visitExecution(Execution execution) {
        return execution.getExpressions().stream().flatMap(this::visitExpr);
    }

    @Override
    public Stream<T> visitInvoke(Invoke invoke) {
        return Stream.concat(
                Stream.concat(
                        visitClassType(invoke.getClassType()),
                        visitMethodPattern(invoke.getMethodPattern())),
                Stream.concat(
                        Optionals.toStream(invoke.getReceiver()).flatMap(this::visitExpr),
                        invoke.getArgs().stream().flatMap(this::visitExpr)));
    }
    
    @Override
    public Stream<T> visitTable(Table table) {
        return Stream.concat(
                table.getHeader().stream().flatMap(this::visitIdent),
                table.getRows().stream().flatMap(this::visitRow));
    }

    @Override
    public Stream<T> visitRow(Row row) {
        return row.getCells().stream().flatMap(this::visitCell);
    }

    @Override
    public Stream<T> visitExprCell(ExprCell exprCell) {
        return visitExpr(exprCell.getExpr());
    }

    @Override
    public Stream<T> visitPredCell(PredCell predCell) {
        return visitPredicate(predCell.getPredicate());
    }

    @Override
    public Stream<T> visitCodeBlock(CodeBlock codeBlock) {
        return visitHeading(codeBlock.getHeading());
    }

    @Override
    public Stream<T> visitHeading(Heading heading) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitIdent(Ident ident) {
        return Stream.empty();
    }
}
