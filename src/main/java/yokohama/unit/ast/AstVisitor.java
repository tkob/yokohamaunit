package yokohama.unit.ast;

public abstract class AstVisitor<T> {
    public abstract T visitGroup(Group group);
    public abstract T visitAbbreviation(Abbreviation abbreviation);
    public T visitDefinition(Definition definition) {
        return definition.accept(
            test -> visitTest(test),
            fourPhaseTest -> visitFourPhaseTest(fourPhaseTest),
            table -> visitTable(table),
            codeBlock -> visitCodeBlock(codeBlock),
            heading -> visitHeading(heading));
    }
    public abstract T visitTest(Test test);
    public abstract T visitAssertion(Assertion assertion);
    public abstract T visitProposition(Proposition proposition);
    public T visitPredicate(Predicate predicate) {
        return predicate.accept(
                isPredicate -> visitIsPredicate(isPredicate),
                isNotPredicate -> visitIsNotPredicate(isNotPredicate),
                throwsPredicate -> visitThrowsPredicate(throwsPredicate));
    };
    public abstract T visitIsPredicate(IsPredicate isPredicate);
    public abstract T visitIsNotPredicate(IsNotPredicate isNotPredicate);
    public abstract T visitThrowsPredicate(ThrowsPredicate throwsPredicate);
    public T visitMatcher(Matcher matcher) {
        return matcher.accept(
                equalTo -> visitEqualToMatcher(equalTo),
                instanceOf -> visitInstanceOfMatcher(instanceOf),
                instanceSuchThat -> visitInstanceSuchThatMatcher(instanceSuchThat),
                nullValue -> visitNullValueMatcher(nullValue));
    }
    public abstract T visitEqualToMatcher(EqualToMatcher equalTo);
    public abstract T visitInstanceOfMatcher(InstanceOfMatcher instanceOf);
    public abstract T visitInstanceSuchThatMatcher(InstanceSuchThatMatcher instanceSuchThat);
    public abstract T visitNullValueMatcher(NullValueMatcher nullValue);

    public T visitExpr(Expr expr) {
        return expr.accept(
                quotedExpr -> visitQuotedExpr(quotedExpr),
                stubExpr -> visitStubExpr(stubExpr),
                invocationExpr -> visitInvocationExpr(invocationExpr),
                integerExpr -> visitIntegerExpr(integerExpr),
                floatingPointExpr -> visitFloatingPointExpr(floatingPointExpr),
                booleanExpr -> visitBooleanExpr(booleanExpr),
                charExpr -> visitCharExpr(charExpr),
                stringExpr -> visitStringExpr(stringExpr),
                anchorExpr -> visitAnchorExpr(anchorExpr));
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
    public abstract T visitStubBehavior(StubBehavior behavior);
    public abstract T visitMethodPattern(MethodPattern methodPattern);
    public abstract T visitType(Type type);
    public T visitNonArrayType(NonArrayType nonArrayType) {
        return nonArrayType.accept(
                primitiveType -> visitPrimitiveType(primitiveType),
                classType -> visitClassType(classType));
    }
    public abstract T visitPrimitiveType(PrimitiveType primitiveType);
    public abstract T visitClassType(ClassType classType);
    public T visitFixture(Fixture fixture) {
        return fixture.accept(
                () -> visitNone(),
                tableRef -> visitTableRef(tableRef),
                bindings -> visitBindings(bindings));
    }
    public abstract T visitNone();
    public abstract T visitTableRef(TableRef tableRef);
    public abstract T visitBindings(Bindings bindings);
    public abstract T visitBinding(Binding binding);
    public abstract T visitFourPhaseTest(FourPhaseTest fourPhaseTest);
    public abstract T visitPhase(Phase phase);
    public abstract T visitVerifyPhase(VerifyPhase verifyPhase);
    public abstract T visitLetStatement(LetStatement letStatement);
    public abstract T visitLetBinding(LetBinding letBinding);
    public abstract T visitExecution(Execution execution);
    public abstract T visitTable(Table table);
    public abstract T visitRow(Row row);
    public T visitCell(Cell cell) {
        return cell.accept(
                exprCell -> visitExprCell(exprCell),
                predCell -> visitPredCell(predCell));
    }
    public abstract T visitExprCell(ExprCell exprCell);
    public abstract T visitPredCell(PredCell predCell);
    public abstract T visitCodeBlock(CodeBlock codeBlock);
    public abstract T visitHeading(Heading heading);
    public abstract T visitIdent(Ident ident);
}
