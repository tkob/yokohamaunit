package yokohama.unit.ast;

public abstract class AstVisitor<T> {
    public abstract T visitGroup(Group group);
    public abstract T visitAbbreviation(Abbreviation abbreviation);
    public T visitDefinition(Definition definition) {
        return definition.accept(
                this::visitTest,
                this::visitFourPhaseTest,
                this::visitTable,
                this::visitCodeBlock,
                this::visitHeading);
    }
    public abstract T visitTest(Test test);
    public abstract T visitAssertion(Assertion assertion);
    public abstract T visitClause(Clause clause);
    public abstract T visitProposition(Proposition proposition);
    public T visitPredicate(Predicate predicate) {
        return predicate.accept(
                this::visitIsPredicate,
                this::visitIsNotPredicate,
                this::visitThrowsPredicate,
                this::visitMatchesPredicate,
                this::visitDoesNotMatchPredicate);
    };
    public abstract T visitIsPredicate(IsPredicate isPredicate);
    public abstract T visitIsNotPredicate(IsNotPredicate isNotPredicate);
    public abstract T visitThrowsPredicate(ThrowsPredicate throwsPredicate);
    public abstract T visitMatchesPredicate(MatchesPredicate matchesPredicate);
    public abstract T visitDoesNotMatchPredicate(DoesNotMatchPredicate doesNotMatchPredicate);
    public T visitMatcher(Matcher matcher) {
        return matcher.accept(
                this::visitEqualToMatcher,
                this::visitInstanceOfMatcher,
                this::visitInstanceSuchThatMatcher,
                this::visitNullValueMatcher);
    }
    public abstract T visitEqualToMatcher(EqualToMatcher equalTo);
    public abstract T visitInstanceOfMatcher(InstanceOfMatcher instanceOf);
    public abstract T visitInstanceSuchThatMatcher(InstanceSuchThatMatcher instanceSuchThat);
    public abstract T visitNullValueMatcher(NullValueMatcher nullValue);
    public T visitPattern(Pattern pattern) {
        return pattern.accept((java.util.function.Function<RegExpPattern, T>)(this::visitRegExpPattern));
    }
    public abstract T visitRegExpPattern(RegExpPattern regExpPattern);
    public T visitExpr(Expr expr) {
        return expr.accept(
                this::visitQuotedExpr,
                this::visitStubExpr,
                this::visitInvocationExpr,
                this::visitIntegerExpr,
                this::visitFloatingPointExpr,
                this::visitBooleanExpr,
                this::visitCharExpr,
                this::visitStringExpr,
                this::visitAnchorExpr,
                this::visitAsExpr,
                this::visitResourceExpr,
                this::visitTempFileExpr);
    }
    public abstract T visitQuotedExpr(QuotedExpr quotedExpr);
    public abstract T visitStubExpr(StubExpr stubExpr);
    public abstract T visitInvocationExpr(InvocationExpr invocationExpr);
    public abstract T visitIntegerExpr(IntegerExpr integerExpr);
    public abstract T visitFloatingPointExpr(FloatingPointExpr floatingPointExpr);
    public abstract T visitBooleanExpr(BooleanExpr booleanExpr);
    public abstract T visitCharExpr(CharExpr charExpr);
    public abstract T visitStringExpr(StringExpr stringExpr);
    public abstract T visitAnchorExpr(AnchorExpr anchorExpr);
    public abstract T visitAsExpr(AsExpr asExpr);
    public abstract T visitResourceExpr(ResourceExpr resourceExpr);
    public abstract T visitTempFileExpr(TempFileExpr tempFileExpr);
    public T visitStubBehavior(StubBehavior behavior) {
        return behavior.accept(
                this::visitStubReturns,
                this::visitStubThrows);
    }
    public abstract T visitStubReturns(StubReturns stubReturns);
    public abstract T visitStubThrows(StubThrows stubThrows);
    public abstract T visitMethodPattern(MethodPattern methodPattern);
    public abstract T visitType(Type type);
    public T visitNonArrayType(NonArrayType nonArrayType) {
        return nonArrayType.accept(
                this::visitPrimitiveType,
                this::visitClassType);
    }
    public abstract T visitPrimitiveType(PrimitiveType primitiveType);
    public abstract T visitClassType(ClassType classType);
    public T visitFixture(Fixture fixture) {
        return fixture.accept(
                this::visitNone,
                this::visitTableRef,
                this::visitBindings);
    }
    public abstract T visitNone();
    public abstract T visitTableRef(TableRef tableRef);
    public abstract T visitBindings(Bindings bindings);
    public T visitBinding(Binding binding) {
        return binding.accept(
                this::visitSingleBinding,
                this::visitChoiceBinding,
                this::visitTableBinding);
    }
    public abstract T visitSingleBinding(SingleBinding singleBinding);
    public abstract T visitChoiceBinding(ChoiceBinding choiceBinding);
    public abstract T visitTableBinding(TableBinding tableBinding);
    public abstract T visitFourPhaseTest(FourPhaseTest fourPhaseTest);
    public abstract T visitPhase(Phase phase);
    public abstract T visitVerifyPhase(VerifyPhase verifyPhase);
    public abstract T visitLetStatement(LetStatement letStatement);
    public T visitStatement(Statement statement) {
        return statement.accept(
                this::visitExecution,
                this::visitInvoke);
    }
    public abstract T visitExecution(Execution execution);
    public abstract T visitInvoke(Invoke invoke);
    public abstract T visitTable(Table table);
    public abstract T visitRow(Row row);
    public T visitCell(Cell cell) {
        return cell.accept(
                this::visitExprCell,
                this::visitPredCell);
    }
    public abstract T visitExprCell(ExprCell exprCell);
    public abstract T visitPredCell(PredCell predCell);
    public abstract T visitCodeBlock(CodeBlock codeBlock);
    public abstract T visitHeading(Heading heading);
    public abstract T visitIdent(Ident ident);
}
