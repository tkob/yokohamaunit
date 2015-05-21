package yokohama.unit.ast;

import java.util.stream.Stream;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.util.Optionals;

public class CheckVisitorTemplate extends AstVisitor<Stream<ErrorMessage>> {
    @Override
    public Stream<ErrorMessage> visitGroup(Group group) {
        return Stream.concat(
                group.getAbbreviations().stream().flatMap(this::visitAbbreviation),
                group.getDefinitions().stream().flatMap(this::visitDefinition));
    }

    @Override
    public Stream<ErrorMessage> visitAbbreviation(Abbreviation abbreviation) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitTest(Test test) {
        return test.getAssertions().stream().flatMap(this::visitAssertion);
    }

    @Override
    public Stream<ErrorMessage> visitAssertion(Assertion assertion) {
        return Stream.concat(
                assertion.getPropositions().stream().flatMap(this::visitProposition),
                visitFixture(assertion.getFixture()));
    }

    @Override
    public Stream<ErrorMessage> visitProposition(Proposition proposition) {
        return Stream.concat(
                visitExpr(proposition.getSubject()),
                visitPredicate(proposition.getPredicate()));
    }

    @Override
    public Stream<ErrorMessage> visitIsPredicate(IsPredicate isPredicate) {
        return visitMatcher(isPredicate.getComplement());
    }

    @Override
    public Stream<ErrorMessage> visitIsNotPredicate(IsNotPredicate isNotPredicate) {
        return visitMatcher(isNotPredicate.getComplement());
    }

    @Override
    public Stream<ErrorMessage> visitThrowsPredicate(ThrowsPredicate throwsPredicate) {
        return visitMatcher(throwsPredicate.getThrowee());
    }

    @Override
    public Stream<ErrorMessage> visitEqualToMatcher(EqualToMatcher equalTo) {
        return visitExpr(equalTo.getExpr());
    }

    @Override
    public Stream<ErrorMessage> visitInstanceOfMatcher(InstanceOfMatcher instanceOf) {
        return visitClassType(instanceOf.getClazz());
    }

    @Override
    public Stream<ErrorMessage> visitInstanceSuchThatMatcher(InstanceSuchThatMatcher instanceSuchThat) {
        return Stream.concat(
                Stream.concat(
                        visitClassType(instanceSuchThat.getClazz()),
                        visitIdent(instanceSuchThat.getVar())),
                instanceSuchThat.getPropositions().stream().flatMap(this::visitProposition));
    }

    @Override
    public Stream<ErrorMessage> visitNullValueMatcher(NullValueMatcher nullValue) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitQuotedExpr(QuotedExpr quotedExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitStubExpr(StubExpr stubExpr) {
        return Stream.concat(
                visitClassType(stubExpr.getClassToStub()),
                stubExpr.getBehavior().stream().flatMap(this::visitStubBehavior));
    }

    @Override
    public Stream<ErrorMessage> visitInvocationExpr(InvocationExpr invocationExpr) {
        return Stream.concat(
                Stream.concat(
                        visitClassType(invocationExpr.getClassType()),
                        visitMethodPattern(invocationExpr.getMethodPattern())),
                Stream.concat(
                        Optionals.toStream(invocationExpr.getReceiver()).flatMap(this::visitExpr),
                        invocationExpr.getArgs().stream().flatMap(this::visitExpr)));
    }

    @Override
    public Stream<ErrorMessage> visitIntegerExpr(IntegerExpr integerExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitFloatingPointExpr(FloatingPointExpr floatingPointExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitBooleanExpr(BooleanExpr booleanExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitCharExpr(CharExpr charExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitStringExpr(StringExpr stringExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitAnchorExpr(AnchorExpr anchorExpr) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitStubBehavior(StubBehavior behavior) {
        return Stream.concat(
                visitMethodPattern(behavior.getMethodPattern()),
                visitExpr(behavior.getToBeReturned()));
    }

    @Override
    public Stream<ErrorMessage> visitMethodPattern(MethodPattern methodPattern) {
        return methodPattern.getParamTypes().stream().flatMap(this::visitType);
    }

    @Override
    public Stream<ErrorMessage> visitType(Type type) {
        return visitNonArrayType(type.getNonArrayType());
    }

    @Override
    public Stream<ErrorMessage> visitPrimitiveType(PrimitiveType primitiveType) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitClassType(ClassType classType) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitNone() {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitTableRef(TableRef tableRef) {
        return tableRef.getIdents().stream().flatMap(this::visitIdent);
    }

    @Override
    public Stream<ErrorMessage> visitBindings(Bindings bindings) {
        return bindings.getBindings().stream().flatMap(this::visitBinding);
    }

    @Override
    public Stream<ErrorMessage> visitSingleBinding(SingleBinding singleBinding) {
        return Stream.concat(
                visitIdent(singleBinding.getName()),
                visitExpr(singleBinding.getValue()));
    }

    @Override
    public Stream<ErrorMessage> visitChoiceBinding(ChoiceBinding choiceBinding) {
        return Stream.concat(
                visitIdent(choiceBinding.getName()),
                choiceBinding.getChoices().stream().flatMap(this::visitExpr));
    }

    @Override
    public Stream<ErrorMessage> visitFourPhaseTest(FourPhaseTest fourPhaseTest) {
        return Stream.concat(
                Stream.concat(
                        Optionals.toStream(fourPhaseTest.getSetup()).flatMap(this::visitPhase),
                        Optionals.toStream(fourPhaseTest.getExercise()).flatMap(this::visitPhase)),
                Stream.concat(
                        visitVerifyPhase(fourPhaseTest.getVerify()),
                        Optionals.toStream(fourPhaseTest.getTeardown()).flatMap(this::visitPhase)));
    }

    @Override
    public Stream<ErrorMessage> visitPhase(Phase phase) {
        return Stream.concat(
                phase.getLetStatements().stream().flatMap(this::visitLetStatement),
                phase.getStatements().stream().flatMap(this::visitStatement));
    }

    @Override
    public Stream<ErrorMessage> visitVerifyPhase(VerifyPhase verifyPhase) {
        return verifyPhase.getAssertions().stream().flatMap(this::visitAssertion);
    }

    @Override
    public Stream<ErrorMessage> visitLetStatement(LetStatement letStatement) {
        return letStatement.getBindings().stream().flatMap(this::visitBinding);
    }

    @Override
    public Stream<ErrorMessage> visitExecution(Execution execution) {
        return execution.getExpressions().stream().flatMap(this::visitExpr);
    }

    @Override
    public Stream<ErrorMessage> visitInvoke(Invoke invoke) {
        return Stream.concat(
                Stream.concat(
                        visitClassType(invoke.getClassType()),
                        visitMethodPattern(invoke.getMethodPattern())),
                Stream.concat(
                        Optionals.toStream(invoke.getReceiver()).flatMap(this::visitExpr),
                        invoke.getArgs().stream().flatMap(this::visitExpr)));
    }
    
    @Override
    public Stream<ErrorMessage> visitTable(Table table) {
        return Stream.concat(
                table.getHeader().stream().flatMap(this::visitIdent),
                table.getRows().stream().flatMap(this::visitRow));
    }

    @Override
    public Stream<ErrorMessage> visitRow(Row row) {
        return row.getCells().stream().flatMap(this::visitCell);
    }

    @Override
    public Stream<ErrorMessage> visitExprCell(ExprCell exprCell) {
        return visitExpr(exprCell.getExpr());
    }

    @Override
    public Stream<ErrorMessage> visitPredCell(PredCell predCell) {
        return visitPredicate(predCell.getPredicate());
    }

    @Override
    public Stream<ErrorMessage> visitCodeBlock(CodeBlock codeBlock) {
        return visitHeading(codeBlock.getHeading());
    }

    @Override
    public Stream<ErrorMessage> visitHeading(Heading heading) {
        return Stream.empty();
    }

    @Override
    public Stream<ErrorMessage> visitIdent(Ident ident) {
        return Stream.empty();
    }
}
