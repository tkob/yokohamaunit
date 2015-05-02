package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import yokohama.unit.ast.Abbreviation;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Binding;
import yokohama.unit.ast.Bindings;
import yokohama.unit.ast.ClassType;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.Expr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.FloatingPointExpr;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.InstanceSuchThatMatcher;
import yokohama.unit.ast.IntegerExpr;
import yokohama.unit.ast.IsNotPredicate;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.LetBinding;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.NonArrayType;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.PrimitiveType;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast.Matcher;
import yokohama.unit.ast.NullValueMatcher;
import yokohama.unit.position.Position;
import yokohama.unit.ast.Predicate;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.Row;
import yokohama.unit.position.Span;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast.StubExpr;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.TableType;
import yokohama.unit.ast.Test;
import yokohama.unit.ast.ThrowsPredicate;
import yokohama.unit.ast.Type;
import yokohama.unit.ast.VerifyPhase;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParserVisitor;
import yokohama.unit.util.Pair;

@AllArgsConstructor
public class ParseTreeToAstVisitor extends AbstractParseTreeVisitor<Object> implements YokohamaUnitParserVisitor<Object> 
{
    private final Optional<Path> docyPath;

    public Span getSpan(ParserRuleContext ctx) {
        Token startToken = ctx.getStart();
        Token stopToken = ctx.getStop();
        Position startPosition = new Position(startToken.getLine(), startToken.getCharPositionInLine() + 1);
        Position endPosition = new Position(stopToken.getLine(), stopToken.getCharPositionInLine() + stopToken.getText().length() + 1);
        Span span = new Span(docyPath, startPosition, endPosition);
        return span;
    }
    public Span nodeSpan(TerminalNode terminalNode) {
        Token token = terminalNode.getSymbol();
        Position startPosition = new Position(token.getLine(), token.getCharPositionInLine() + 1);
        Position endPosition = new Position(token.getLine(), token.getCharPositionInLine() + token.getText().length() + 1);
        Span span = new Span(docyPath, startPosition, endPosition);
        return span;
    }

    @Override
    public Group visitGroup(YokohamaUnitParser.GroupContext ctx) {
        List<Abbreviation> abbreviations =
                ctx.abbreviation().stream()
                                .map(this::visitAbbreviation)
                                .collect(Collectors.toList());
        List<Definition>definitions =
                ctx.definition().stream()
                                .map(this::visitDefinition)
                                .collect(Collectors.toList());
        return new Group(abbreviations, definitions, getSpan(ctx));
    }

    @Override
    public Abbreviation visitAbbreviation(YokohamaUnitParser.AbbreviationContext ctx) {
        return new Abbreviation(ctx.ShortName().getText(), ctx.LongName().getText(), getSpan(ctx));
    }

    @Override
    public Definition visitDefinition(YokohamaUnitParser.DefinitionContext ctx) {
        return (Definition)visitChildren(ctx);
    }

    @Override
    public Test visitTest(YokohamaUnitParser.TestContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        String name = ctx.TestName().getText();
        List<Assertion> assertions =
                ctx.assertion().stream()
                               .map(this::visitAssertion)
                               .collect(Collectors.toList());
        return new Test(name, assertions, numHashes, getSpan(ctx));
    }

    @Override
    public Integer visitHash(YokohamaUnitParser.HashContext ctx) {
        return ctx.getText().length();
    }

    @Override
    public Assertion visitAssertion(YokohamaUnitParser.AssertionContext ctx) {
        List<Proposition> propositions = visitPropositions(ctx.propositions());
        YokohamaUnitParser.ConditionContext conditionCtx = (ctx.condition());
        Fixture fixture =
                conditionCtx == null ? Fixture.none()
                                     : visitCondition(ctx.condition());
        return new Assertion(propositions, fixture, getSpan(ctx));
    }

    @Override
    public List<Proposition> visitPropositions(YokohamaUnitParser.PropositionsContext ctx) {
        return ctx.proposition().stream()
                                .map(this::visitProposition)
                                .collect(Collectors.toList());
    }

    @Override
    public Proposition visitProposition(YokohamaUnitParser.PropositionContext ctx) {
        QuotedExpr subject = visitSubject(ctx.subject());
        Predicate predicate = visitPredicate(ctx.predicate());
        return new Proposition(subject, predicate, getSpan(ctx));
    }

    @Override
    public QuotedExpr visitSubject(YokohamaUnitParser.SubjectContext ctx) {
        return new QuotedExpr(ctx.Expr().getText(), nodeSpan(ctx.Expr()));
    }

    @Override
    public Predicate visitPredicate(YokohamaUnitParser.PredicateContext ctx) {
        return (Predicate)visitChildren(ctx);
    }

    @Override
    public IsPredicate visitIsPredicate(YokohamaUnitParser.IsPredicateContext ctx) {
        return new IsPredicate(visitMatcher(ctx.matcher()), getSpan(ctx));
    }

    @Override
    public IsNotPredicate visitIsNotPredicate(YokohamaUnitParser.IsNotPredicateContext ctx) {
        return new IsNotPredicate(visitMatcher(ctx.matcher()), getSpan(ctx));
    }

    @Override
    public ThrowsPredicate visitThrowsPredicate(YokohamaUnitParser.ThrowsPredicateContext ctx) {
        return new ThrowsPredicate(visitMatcher(ctx.matcher()), getSpan(ctx));
    }

    @Override
    public Matcher visitMatcher(YokohamaUnitParser.MatcherContext ctx) {
        return (Matcher)visitChildren(ctx);
    }

    @Override
    public Matcher visitEqualTo(YokohamaUnitParser.EqualToContext ctx) {
        return new EqualToMatcher(new QuotedExpr(ctx.Expr().getText(), nodeSpan(ctx.Expr())), getSpan(ctx));
    }

    @Override
    public Matcher visitInstanceOf(YokohamaUnitParser.InstanceOfContext ctx) {
        return new InstanceOfMatcher(visitClassType(ctx.classType()), getSpan(ctx));
    }

    @Override
    public Matcher visitInstanceSuchThat(YokohamaUnitParser.InstanceSuchThatContext ctx) {
        return new InstanceSuchThatMatcher(
                new Ident(ctx.Identifier().getText(), nodeSpan(ctx.Identifier())),
                visitClassType(ctx.classType()),
                ctx.proposition().stream().map(this::visitProposition).collect(Collectors.toList()),
                getSpan(ctx));
    }

    @Override
    public Matcher visitNullValue(YokohamaUnitParser.NullValueContext ctx) {
        return new NullValueMatcher(getSpan(ctx));
    }

    @Override
    public Fixture visitCondition(YokohamaUnitParser.ConditionContext ctx) {
        return (Fixture)visitChildren(ctx);
    }

    @Override
    public TableRef visitForAll(YokohamaUnitParser.ForAllContext ctx) {
        List<Ident> idents = visitVars(ctx.vars());
        Pair<TableType, String> typeAndName =  visitTableRef(ctx.tableRef());
        TableType tableType = typeAndName.getFirst();
        String name = typeAndName.getSecond();
        return new TableRef(idents, tableType, name, getSpan(ctx));
    }

    @Override
    public List<Ident> visitVars(YokohamaUnitParser.VarsContext ctx) {
        return ctx.Identifier().stream()
                .map(ident -> new Ident(ident.getText(), nodeSpan(ident)))
                .collect(Collectors.toList());
    }

    @Override
    public Pair<TableType, String> visitTableRef(YokohamaUnitParser.TableRefContext ctx) {
        String name = ctx.SingleQuoteName().getText().replace("''", "'");
        if (ctx.UTABLE() != null) {
                return new Pair<>(TableType.INLINE, name);
        } else if (ctx.CSV() != null) {
                return new Pair<>(TableType.CSV, name);
        } else if (ctx.TSV() != null) {
                return new Pair<>(TableType.TSV, name);
        } else if (ctx.EXCEL() != null) {
                return new Pair<>(TableType.EXCEL, name);
        } else {
            throw new IllegalArgumentException("'" + ctx.getText() + "' is not a table reference.");
        }
    }

    @Override
    public Bindings visitBindings(YokohamaUnitParser.BindingsContext ctx) {
        List<Binding> bindings = ctx.binding().stream()
                                              .map(this::visitBinding)
                                              .collect(Collectors.toList());
	return new Bindings(bindings, getSpan(ctx));
   }

    @Override
    public Binding visitBinding(YokohamaUnitParser.BindingContext ctx) {
        Ident ident = new Ident(ctx.Identifier().getText(), nodeSpan(ctx.Identifier()));
        Expr expr = visitExpr(ctx.expr());
        return new Binding(ident, expr, getSpan(ctx));
    }

    @Override
    public Table visitTableDef(YokohamaUnitParser.TableDefContext ctx) {
        String name = ctx.TableName().getText();
        List<Ident> header = visitHeader(ctx.header());
        List<Row> rows = visitRows(ctx.rows());
        return new Table(name, header, rows, getSpan(ctx));
    }

    @Override
    public List<Ident> visitHeader(YokohamaUnitParser.HeaderContext ctx) {
        return ctx.Identifier().stream()
                               .map(ident -> new Ident(ident.getText(), nodeSpan(ident)))
                               .collect(Collectors.toList());
    }

    @Override
    public List<Row> visitRows(YokohamaUnitParser.RowsContext ctx) {
        return ctx.row().stream()
                        .map(this::visitRow)
                        .collect(Collectors.toList());
    }

    @Override
    public Row visitRow(YokohamaUnitParser.RowContext ctx) {
        List<Expr> exprs =
                ctx.Expr().stream()
                          .map(cell ->
                                  new QuotedExpr(cell.getText().trim(), nodeSpan(cell)))
                          .collect(Collectors.toList());
        return new Row(exprs, getSpan(ctx));
    }

    @Override
    public FourPhaseTest visitFourPhaseTest(YokohamaUnitParser.FourPhaseTestContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        String name = ctx.TestName().getText();
        Optional<Phase> setup =
                ctx.setup() == null ? Optional.empty()
                                    : Optional.of(visitSetup(ctx.setup()));
        Optional<Phase> exercise =
                ctx.exercise() == null ? Optional.empty()
                                       : Optional.of(visitExercise(ctx.exercise()));
        VerifyPhase verify = visitVerify(ctx.verify());
        Optional<Phase> teardown =
                ctx.teardown() == null ? Optional.empty()
                                       : Optional.of(visitTeardown(ctx.teardown()));
        return new FourPhaseTest(numHashes, name, setup, exercise, verify, teardown, getSpan(ctx));
    }

    @Override
    public Phase visitSetup(YokohamaUnitParser.SetupContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        Optional<LetBindings> letBindings =
                ctx.letBindings() == null ? Optional.empty()
                                          : Optional.of(visitLetBindings(ctx.letBindings()));
        List<Execution> executions = ctx.execution()
                .stream()
                .map(this::visitExecution)
                .collect(Collectors.toList());
        return new Phase(numHashes, description, letBindings, executions, getSpan(ctx));
    }

    @Override
    public Phase visitExercise(YokohamaUnitParser.ExerciseContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        List<Execution> executions = ctx.execution()
                .stream()
                .map(this::visitExecution)
                .collect(Collectors.toList());
        return new Phase(numHashes, description, Optional.empty(), executions, getSpan(ctx));
    }

    @Override
    public VerifyPhase visitVerify(YokohamaUnitParser.VerifyContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        List<Assertion> assertions = ctx.assertion()
                .stream()
                .map(this::visitAssertion)
                .collect(Collectors.toList());
        return new VerifyPhase(numHashes, description, assertions, getSpan(ctx));
    }

    @Override
    public Phase visitTeardown(YokohamaUnitParser.TeardownContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        List<Execution> executions = ctx.execution()
                .stream()
                .map(this::visitExecution)
                .collect(Collectors.toList());
        return new Phase(numHashes, description, Optional.empty(), executions, getSpan(ctx));
    }

    @Override
    public LetBindings visitLetBindings(YokohamaUnitParser.LetBindingsContext ctx) {
        return new LetBindings(
                ctx.letBinding().stream()
                        .map(this::visitLetBinding)
                        .collect(Collectors.toList()), getSpan(ctx));
    }

    @Override
    public LetBinding visitLetBinding(YokohamaUnitParser.LetBindingContext ctx) {
        return new LetBinding(
                ctx.Identifier().getText(),
                visitExpr(ctx.expr()),
                getSpan(ctx));
    }

    @Override
    public Execution visitExecution(YokohamaUnitParser.ExecutionContext ctx) {
        return new Execution(
                ctx.Expr().stream()
                        .map(expr -> new QuotedExpr(expr.getText(), nodeSpan(expr)))
                        .collect(Collectors.toList()),
                getSpan(ctx));
    }

    @Override
    public Expr visitExpr(YokohamaUnitParser.ExprContext ctx) {
        return ctx.Expr() != null ? new QuotedExpr(ctx.Expr().getText(), nodeSpan(ctx.Expr()))
                                  : (Expr)visitChildren(ctx);
    }

    @Override
    public StubExpr visitStubExpr(YokohamaUnitParser.StubExprContext ctx) {
        ClassType classToStub = visitClassType(ctx.classType());
        List<StubBehavior> behavior =
                ctx.stubBehavior().stream()
                                  .map(this::visitStubBehavior)
                                  .collect(Collectors.toList());
        return new StubExpr(classToStub, behavior, getSpan(ctx));
    }

    @Override
    public StubBehavior visitStubBehavior(YokohamaUnitParser.StubBehaviorContext ctx) {
        MethodPattern methodPattern = visitMethodPattern(ctx.methodPattern());
        Expr toBeReturned = visitExpr(ctx.expr());
        return new StubBehavior(methodPattern, toBeReturned, getSpan(ctx));
    }

    @Override
    public MethodPattern visitMethodPattern(YokohamaUnitParser.MethodPatternContext ctx) {
        String name = ctx.Identifier().getText();
        List<Type> argumentTypes = ctx.type().stream().map(this::visitType).collect(Collectors.toList());
        boolean varArg = ctx.THREEDOTS() != null;
        return new MethodPattern(name, argumentTypes, varArg, getSpan(ctx));
    }

    @Override
    public Type visitType(YokohamaUnitParser.TypeContext ctx) {
        NonArrayType nonArrayType = visitNonArrayType(ctx.nonArrayType());
        int dims = ctx.LBRACKET().size();
        return new Type(nonArrayType, dims, getSpan(ctx));
    }

    @Override
    public NonArrayType visitNonArrayType(YokohamaUnitParser.NonArrayTypeContext ctx) {
        return (NonArrayType)visitChildren(ctx);
    }

    @Override
    public PrimitiveType visitPrimitiveType(YokohamaUnitParser.PrimitiveTypeContext ctx) {
        if (ctx.BOOLEAN() != null) {
            return new PrimitiveType(Kind.BOOLEAN, getSpan(ctx));
        } else if (ctx.BYTE() != null) {
            return new PrimitiveType(Kind.BYTE, getSpan(ctx));
        } else if (ctx.SHORT() != null) {
            return new PrimitiveType(Kind.SHORT, getSpan(ctx));
        } else if (ctx.INT() != null) {
            return new PrimitiveType(Kind.INT, getSpan(ctx));
        } else if (ctx.LONG() != null) {
            return new PrimitiveType(Kind.LONG, getSpan(ctx));
        } else if (ctx.CHAR() != null) {
            return new PrimitiveType(Kind.CHAR, getSpan(ctx));
        } else if (ctx.FLOAT() != null) {
            return new PrimitiveType(Kind.FLOAT, getSpan(ctx));
        } else if (ctx.DOUBLE() != null) {
            return new PrimitiveType(Kind.DOUBLE, getSpan(ctx));
        } else {
            throw new RuntimeException("Shuld not reach here");
        }
    }

    @Override
    public ClassType visitClassType(YokohamaUnitParser.ClassTypeContext ctx) {
        String name = String.join(
                ".",
                ctx.Identifier().stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors.toList())
        );
        return new ClassType(name, getSpan(ctx));
    }

    @Override
    public Object visitInvokeExpr(YokohamaUnitParser.InvokeExprContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitReceiver(YokohamaUnitParser.ReceiverContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntegerExpr visitIntegerExpr(YokohamaUnitParser.IntegerExprContext ctx) {
        boolean positive = ctx.MINUS() == null;
        String literal = ctx.Integer().getText();
        return new IntegerExpr(positive, literal, getSpan(ctx));
    }

    @Override
    public FloatingPointExpr visitFloatingPointExpr(YokohamaUnitParser.FloatingPointExprContext ctx) {
        boolean positive = ctx.MINUS() == null;
        String literal = ctx.FloatingPoint().getText();
        return new FloatingPointExpr(positive, literal, getSpan(ctx));
    }
}
