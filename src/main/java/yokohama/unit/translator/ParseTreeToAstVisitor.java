package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.ast.Abbreviation;
import yokohama.unit.ast.AnchorExpr;
import yokohama.unit.ast.AsExpr;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Binding;
import yokohama.unit.ast.Bindings;
import yokohama.unit.ast.BooleanExpr;
import yokohama.unit.ast.Cell;
import yokohama.unit.ast.CharExpr;
import yokohama.unit.ast.ChoiceBinding;
import yokohama.unit.ast.ClassType;
import yokohama.unit.ast.CodeBlock;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.DoesNotMatchPredicate;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.Expr;
import yokohama.unit.ast.ExprCell;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.FloatingPointExpr;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Heading;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.InstanceSuchThatMatcher;
import yokohama.unit.ast.IntegerExpr;
import yokohama.unit.ast.InvocationExpr;
import yokohama.unit.ast.Invoke;
import yokohama.unit.ast.IsNotPredicate;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.LetStatement;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.NonArrayType;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.PrimitiveType;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast.Matcher;
import yokohama.unit.ast.MatchesPredicate;
import yokohama.unit.ast.NullValueMatcher;
import yokohama.unit.ast.Pattern;
import yokohama.unit.position.Position;
import yokohama.unit.ast.Predicate;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.RegExpPattern;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.SingleBinding;
import yokohama.unit.ast.Statement;
import yokohama.unit.ast.StringExpr;
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
import yokohama.unit.util.Lists;
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
        return new Abbreviation(ctx.ShortName().getText(), ctx.Line().getText(), getSpan(ctx));
    }

    @Override
    public Definition visitDefinition(YokohamaUnitParser.DefinitionContext ctx) {
        return (Definition)visitChildren(ctx);
    }

    @Override
    public Test visitTest(YokohamaUnitParser.TestContext ctx) {
        String name = ctx.Line().getText();
        List<Assertion> assertions =
                ctx.assertion().stream()
                               .map(this::visitAssertion)
                               .collect(Collectors.toList());
        return new Test(name, assertions, getSpan(ctx));
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
        Expr subject = visitSubject(ctx.subject());
        Predicate predicate = visitPredicate(ctx.predicate());
        return new Proposition(subject, predicate, getSpan(ctx));
    }

    @Override
    public Expr visitSubject(YokohamaUnitParser.SubjectContext ctx) {
        return (Expr)visitChildren(ctx);
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
    public MatchesPredicate visitMatchesPredicate(
            YokohamaUnitParser.MatchesPredicateContext ctx) {
        return new MatchesPredicate(visitPattern(ctx.pattern()), getSpan(ctx));
    }

    @Override
    public DoesNotMatchPredicate visitDoesNotMatchPredicate(
            YokohamaUnitParser.DoesNotMatchPredicateContext ctx) {
        return new DoesNotMatchPredicate(
                visitPattern(ctx.pattern()), getSpan(ctx));
    }

    @Override
    public Matcher visitMatcher(YokohamaUnitParser.MatcherContext ctx) {
        return (Matcher)visitChildren(ctx);
    }

    @Override
    public Matcher visitEqualTo(YokohamaUnitParser.EqualToContext ctx) {
        return new EqualToMatcher(
                visitArgumentExpr(ctx.argumentExpr()), getSpan(ctx));
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
    public Pattern visitPattern(YokohamaUnitParser.PatternContext ctx) {
        return (Pattern)visitChildren(ctx); 
    }

    @Override
    public RegExpPattern visitRegexp(YokohamaUnitParser.RegexpContext ctx) {
        return new RegExpPattern(ctx.Regexp().getText(), getSpan(ctx));
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
        if (ctx.UTABLE() != null) {
            String name = ctx.Anchor().getText();
            return new Pair<>(TableType.INLINE, name);
        } else if (ctx.CSV_SINGLE_QUOTE() != null) {
            String name = ctx.FileName().getText().replace("''", "'");
            return new Pair<>(TableType.CSV, name);
        } else if (ctx.TSV_SINGLE_QUOTE() != null) {
            String name = ctx.FileName().getText().replace("''", "'");
            return new Pair<>(TableType.TSV, name);
        } else if (ctx.EXCEL_SINGLE_QUOTE() != null) {
            String name = ctx.BookName().getText().replace("''", "'");
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
        return (Binding)visitChildren(ctx);
    }

    @Override
    public SingleBinding visitSingleBinding(YokohamaUnitParser.SingleBindingContext ctx) {
        Ident ident = new Ident(ctx.Identifier().getText(), nodeSpan(ctx.Identifier()));
        Expr expr = visitExpr(ctx.expr());
        return new SingleBinding(ident, expr, getSpan(ctx));
    }

    @Override
    public ChoiceBinding visitChoiceBinding(YokohamaUnitParser.ChoiceBindingContext ctx) {
        Ident ident = new Ident(ctx.Identifier().getText(), nodeSpan(ctx.Identifier()));
        List<Expr> exprs = ctx.expr().stream().map(this::visitExpr).collect(Collectors.toList());
        return new ChoiceBinding(ident, exprs, getSpan(ctx));
    }

    @Override
    public Table visitTableDef(YokohamaUnitParser.TableDefContext ctx) {
        String name = ctx.Anchor().getText();
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
        List<Cell> cells =
                ctx.argumentExpr().stream()
                        .map(expr ->
                                new ExprCell(
                                        visitArgumentExpr(expr), getSpan(expr)))
                        .collect(Collectors.toList());
        return new Row(cells, getSpan(ctx));
    }

    @Override
    public FourPhaseTest visitFourPhaseTest(YokohamaUnitParser.FourPhaseTestContext ctx) {
        String name = ctx.Line().getText();
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
        return new FourPhaseTest(name, setup, exercise, verify, teardown, getSpan(ctx));
    }

    @Override
    public Phase visitSetup(YokohamaUnitParser.SetupContext ctx) {
        Optional<String> description =
                ctx.Line() == null ? Optional.empty()
                                               : Optional.of(ctx.Line().getText());
        List<LetStatement> letStatements =
                ctx.letStatement().stream()
                        .map(letStatement -> visitLetStatement(letStatement))
                        .collect(Collectors.toList());

        List<Statement> statements = ctx.statement()
                .stream()
                .map(this::visitStatement)
                .collect(Collectors.toList());
        return new Phase(description, letStatements, statements, getSpan(ctx));
    }

    @Override
    public Phase visitExercise(YokohamaUnitParser.ExerciseContext ctx) {
        Optional<String> description =
                ctx.Line() == null ? Optional.empty()
                                               : Optional.of(ctx.Line().getText());
        List<Statement> statements = ctx.statement()
                .stream()
                .map(this::visitStatement)
                .collect(Collectors.toList());
        return new Phase(description, Collections.emptyList(), statements, getSpan(ctx));
    }

    @Override
    public VerifyPhase visitVerify(YokohamaUnitParser.VerifyContext ctx) {
        Optional<String> description =
                ctx.Line() == null ? Optional.empty()
                                               : Optional.of(ctx.Line().getText());
        List<Assertion> assertions = ctx.assertion()
                .stream()
                .map(this::visitAssertion)
                .collect(Collectors.toList());
        return new VerifyPhase(description, assertions, getSpan(ctx));
    }

    @Override
    public Phase visitTeardown(YokohamaUnitParser.TeardownContext ctx) {
        Optional<String> description =
                ctx.Line() == null ? Optional.empty()
                                               : Optional.of(ctx.Line().getText());
        List<Statement> statements = ctx.statement()
                .stream()
                .map(this::visitStatement)
                .collect(Collectors.toList());
        return new Phase(description, Collections.emptyList(), statements, getSpan(ctx));
    }

    @Override
    public LetStatement visitLetStatement(YokohamaUnitParser.LetStatementContext ctx) {
        return new LetStatement(
                ctx.letBinding().stream()
                        .map(this::visitLetBinding)
                        .collect(Collectors.toList()), getSpan(ctx));
    }

    @Override
    public Binding visitLetBinding(YokohamaUnitParser.LetBindingContext ctx) {
        return (Binding)visitChildren(ctx);
    }

    @Override
    public SingleBinding visitLetSingleBinding(YokohamaUnitParser.LetSingleBindingContext ctx) {
        return new SingleBinding(
                new Ident(ctx.Identifier().getText(), nodeSpan(ctx.Identifier())),
                visitExpr(ctx.expr()),
                getSpan(ctx));
    }

    @Override
    public ChoiceBinding visitLetChoiceBinding(YokohamaUnitParser.LetChoiceBindingContext ctx) {
        Ident ident = new Ident(ctx.Identifier().getText(), nodeSpan(ctx.Identifier()));
        List<Expr> exprs = ctx.expr().stream().map(this::visitExpr).collect(Collectors.toList());
        return new ChoiceBinding(ident, exprs, getSpan(ctx));
    }

    @Override
    public Statement visitStatement(YokohamaUnitParser.StatementContext ctx) {
        return (Statement)visitChildren(ctx);
    }

    @Override
    public Execution visitExecution(YokohamaUnitParser.ExecutionContext ctx) {
        return new Execution(
                ctx.quotedExpr().stream()
                        .map(quotedExpr -> visitQuotedExpr(quotedExpr))
                        .collect(Collectors.toList()),
                getSpan(ctx));
    }

    @Override
    public Invoke visitInvoke(YokohamaUnitParser.InvokeContext ctx) {
        ClassType classType = visitClassType(ctx.classType());
        MethodPattern methodPattern = visitMethodPattern(ctx.methodPattern());
        Optional<Expr> receiver = Optional.ofNullable(ctx.quotedExpr())
                        .map(quotedExpr -> visitQuotedExpr(quotedExpr));
        List<Expr> args = ctx.argumentExpr().stream()
                .map(this::visitArgumentExpr)
                .collect(Collectors.toList());
        return new Invoke(classType, methodPattern, receiver, args, getSpan(ctx));
    }

    @Override
    public Expr visitExpr(YokohamaUnitParser.ExprContext ctx) {
        return (Expr)visitChildren(ctx);
    }

    @Override
    public QuotedExpr visitQuotedExpr(YokohamaUnitParser.QuotedExprContext ctx) {
        return new QuotedExpr(ctx.Expr().getText(), nodeSpan(ctx.Expr()));
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
        return (StubBehavior)visitChildren(ctx);
    }

    @Override
    public StubBehavior visitStubReturns(YokohamaUnitParser.StubReturnsContext ctx) {
        MethodPattern methodPattern = visitMethodPattern(ctx.methodPattern());
        Expr toBeReturned = visitExpr(ctx.expr());
        return new StubBehavior(methodPattern, toBeReturned, getSpan(ctx));
    }

    @Override
    public Object visitStubThrows(YokohamaUnitParser.StubThrowsContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public InvocationExpr visitInvokeExpr(YokohamaUnitParser.InvokeExprContext ctx) {
        ClassType classType = visitClassType(ctx.classType());
        MethodPattern methodPattern = visitMethodPattern(ctx.methodPattern());
        Optional<Expr> receiver = Optional.ofNullable(ctx.quotedExpr())
                        .map(quotedExpr -> visitQuotedExpr(quotedExpr));
        List<Expr> args = ctx.argumentExpr().stream()
                .map(this::visitArgumentExpr)
                .collect(Collectors.toList());
        return new InvocationExpr(classType, methodPattern, receiver, args, getSpan(ctx));
    }

    @Override
    public Expr visitArgumentExpr(YokohamaUnitParser.ArgumentExprContext ctx) {
        return (Expr)visitChildren(ctx);
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

    @Override
    public BooleanExpr visitBooleanExpr(YokohamaUnitParser.BooleanExprContext ctx) {
        return new BooleanExpr(ctx.FALSE() == null, getSpan(ctx));
    }

    @Override
    public CharExpr visitCharExpr(YokohamaUnitParser.CharExprContext ctx) {
        return new CharExpr(ctx.Char().getText(), getSpan(ctx));
    }

    @Override
    public StringExpr visitStringExpr(YokohamaUnitParser.StringExprContext ctx) {
        String literal = ctx.EMPTY_STRING() != null ? "" : ctx.Str().getText();
        return new StringExpr(literal, getSpan(ctx));
    }

    @Override
    public AnchorExpr visitAnchorExpr(YokohamaUnitParser.AnchorExprContext ctx) {
        return new AnchorExpr(ctx.Anchor().getText(), getSpan(ctx));
    }


    @Override
    public AsExpr visitAsExpr(YokohamaUnitParser.AsExprContext ctx) {
        return new AsExpr(
                visitSourceExpr(ctx.sourceExpr()),
                visitClassType(ctx.classType()),
                getSpan(ctx));
    }

    @Override
    public Expr visitSourceExpr(YokohamaUnitParser.SourceExprContext ctx) {
        return (Expr)visitChildren(ctx);
    }

    @Override
    public Heading visitHeading(YokohamaUnitParser.HeadingContext ctx) {
        return new Heading(ctx.Line().getText(), getSpan(ctx));
    }

    @Override
    public CodeBlock visitCodeBlock(YokohamaUnitParser.CodeBlockContext ctx) {
        return new CodeBlock(
                visitHeading(ctx.heading()),
                visitAttributes(ctx.attributes()),
                Lists.map(
                        ctx.CodeLine(),
                        codeLine -> StringUtils.chomp(codeLine.getText())),
                getSpan(ctx));
    }

    @Override
    public List<String> visitAttributes(YokohamaUnitParser.AttributesContext ctx) {
        return Arrays.asList(StringUtils.split(ctx.CodeLine().getText()));
    }
}
