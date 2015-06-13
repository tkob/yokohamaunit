package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Binding;
import yokohama.unit.ast.Bindings;
import yokohama.unit.ast.BooleanExpr;
import yokohama.unit.ast.ClassType;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.ExprCell;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.IntegerExpr;
import yokohama.unit.ast.InvocationExpr;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast.LetStatement;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.PrimitiveType;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.SingleBinding;
import yokohama.unit.ast.StringExpr;
import yokohama.unit.position.Span;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.TableType;
import yokohama.unit.ast.Type;
import yokohama.unit.ast.VerifyPhase;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.util.Pair;

public class ParseTreeToAstVisitorTest {

    public static YokohamaUnitParser parser(String input) throws IOException {
        return parser(input, YokohamaUnitLexer.DEFAULT_MODE);
    }

    public static YokohamaUnitParser parser(String input, int mode) throws IOException {
        InputStream bais = new ByteArrayInputStream(input.getBytes());
        CharStream stream = new ANTLRInputStream(bais);
        Lexer lex = new YokohamaUnitLexer(stream);
        lex.mode(mode);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        YokohamaUnitParser parser = new YokohamaUnitParser(tokens);
        return parser;
    }

    @Test
    public void testVisitGroup() throws IOException {
        YokohamaUnitParser.GroupContext ctx = parser("# Test: test name\nAssert `a` is `b`.").group();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Group result = instance.visitGroup(ctx);
        assertThat(result.getDefinitions().size(), is(1));
    }

    @Test
    public void testVisitDefinition() throws IOException {
        YokohamaUnitParser.DefinitionContext ctx = parser("# Test: test name\n Assert `a` is `b`.").definition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Definition result = instance.visitDefinition(ctx);
        assertThat(result, is(instanceOf(yokohama.unit.ast.Test.class)));
    }

    @Test
    public void testVisitDefinition2() throws IOException {
        YokohamaUnitParser.DefinitionContext ctx = parser("[table name]\n|a|b|\n|----|\n|1|2|\n\n").definition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Definition result = instance.visitDefinition(ctx);
        assertThat(result, is(instanceOf(Table.class)));
    }

    @Test
    public void testVisitTest() throws IOException {
        YokohamaUnitParser.TestContext ctx = parser("# Test: test name\nAssert `1` is `1`.").test();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        yokohama.unit.ast.Test result = instance.visitTest(ctx);
        assertEquals("test name", result.getName());
    }

    @Test
    public void testVisitAssertion() throws IOException {
        YokohamaUnitParser.AssertionContext ctx = parser("Assert `a` is `b`.").assertion();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Assertion actual = instance.visitAssertion(ctx);
        Assertion expected = new Assertion(
                Arrays.asList(
                        new Proposition(
                                new QuotedExpr("a", Span.dummySpan()),
                                new IsPredicate(
                                        new EqualToMatcher(
                                                new QuotedExpr("b", Span.dummySpan()),
                                                Span.dummySpan()),
                                        Span.dummySpan()),
                                Span.dummySpan())),
                Fixture.none(),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitAssertion2() throws IOException {
        YokohamaUnitParser.AssertionContext ctx = parser("Assert `x` is `y` where x = `1`.").assertion();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Assertion actual = instance.visitAssertion(ctx);
        Assertion expected = new Assertion(
                Arrays.asList(
                        new Proposition(
                                new QuotedExpr("x", Span.dummySpan()),
                                new IsPredicate(
                                        new EqualToMatcher(
                                                new QuotedExpr("y", Span.dummySpan()),
                                                Span.dummySpan()),
                                        Span.dummySpan()),
                                Span.dummySpan())),
                new Bindings(
                        Arrays.asList(
                                new SingleBinding(
                                        new Ident("x", Span.dummySpan()),
                                        new QuotedExpr("1", Span.dummySpan()),
                                        Span.dummySpan())),
                        Span.dummySpan()),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitBindings() throws IOException {
        YokohamaUnitParser.BindingsContext ctx = parser("where x = `1`").bindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Bindings result = instance.visitBindings(ctx);
        assertThat(result.getBindings().size(), is(1));
    }

    @Test
    public void testVisitBindings2() throws IOException {
        YokohamaUnitParser.BindingsContext ctx = parser("where x = `1` and y = `2`").bindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Bindings result = instance.visitBindings(ctx);
        assertThat(result.getBindings().size(), is(2));
    }

    @Test
    public void testVisitBinding() throws IOException {
        YokohamaUnitParser.BindingContext ctx = parser("x = `1`").binding();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Binding actual = instance.visitBinding(ctx);
        Binding expected = new SingleBinding(
                new Ident("x", Span.dummySpan()),
                new QuotedExpr("1", Span.dummySpan()), Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitCondition() throws IOException {
        YokohamaUnitParser.ConditionContext ctx = parser("for all var in Table [table 1]").condition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Fixture result = instance.visitCondition(ctx);
        assertThat(result, is(instanceOf(TableRef.class)));
    }

    @Test
    public void testVisitCondition2() throws IOException {
        YokohamaUnitParser.ConditionContext ctx = parser("where x = `1`").condition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Fixture result = instance.visitCondition(ctx);
        assertThat(result, is(instanceOf(Bindings.class)));
    }

    @Test
    public void testVisitForAll() throws IOException {
        YokohamaUnitParser.ForAllContext ctx = parser("for all var in Table [table name]").forAll();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        TableRef result = instance.visitForAll(ctx);
        assertEquals("table name", result.getName());
    }

    @Test
    public void testVisitTableRef() throws IOException {
        YokohamaUnitParser.TableRefContext ctx = parser("Table [a'b]").tableRef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Pair<TableType, String> expected = new Pair<>(TableType.INLINE, "a'b");
        Pair<TableType, String> actual = instance.visitTableRef(ctx);
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTableRef2() throws IOException {
        YokohamaUnitParser.TableRefContext ctx = parser("CSV 'a''b'").tableRef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Pair<TableType, String> expected = new Pair<>(TableType.CSV, "a'b");
        Pair<TableType, String> actual = instance.visitTableRef(ctx);
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTableRef3() throws IOException {
        YokohamaUnitParser.TableRefContext ctx = parser("TSV 'a''b'").tableRef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Pair<TableType, String> expected = new Pair<>(TableType.TSV, "a'b");
        Pair<TableType, String> actual = instance.visitTableRef(ctx);
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTableRef4() throws IOException {
        YokohamaUnitParser.TableRefContext ctx = parser("Excel 'a''b'").tableRef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Pair<TableType, String> expected = new Pair<>(TableType.EXCEL, "a'b");
        Pair<TableType, String> actual = instance.visitTableRef(ctx);
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitClauses() throws IOException {
        YokohamaUnitParser.ClausesContext ctx = parser("`a` is `b`").clauses();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Proposition> result = instance.visitClauses(ctx);
        assertThat(result.size(), is(1));
    }

    @Test
    public void testVisitClauses2() throws IOException {
        YokohamaUnitParser.ClausesContext ctx = parser("`a` is `b` and `c` is `d`").clauses();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Proposition> result = instance.visitClauses(ctx);
        assertThat(result.size(), is(2));
    }

    @Test
    public void testVisitProposition() throws IOException {
        YokohamaUnitParser.PropositionContext ctx = parser("`a` is `b`").proposition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Proposition actual = instance.visitProposition(ctx);
        Proposition expected =
                new Proposition(
                        new QuotedExpr("a", Span.dummySpan()),
                        new IsPredicate(
                                new EqualToMatcher(
                                        new QuotedExpr("b", Span.dummySpan()),
                                        Span.dummySpan()),
                                Span.dummySpan()),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTableDef() throws IOException {
        YokohamaUnitParser.TableDefContext ctx = parser("[table name]\n|a|b|\n|`1`|2|\n").tableDef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Table actual = instance.visitTableDef(ctx);
        Table expected = new Table(
                "table name",
                Arrays.asList(new Ident("a", Span.dummySpan()), new Ident("b", Span.dummySpan())),
                Arrays.asList(
                        new Row(
                                Arrays.asList(
                                        new ExprCell(new QuotedExpr("1", Span.dummySpan()), Span.dummySpan()),
                                        new ExprCell(new IntegerExpr(true, "2", Span.dummySpan()), Span.dummySpan())),
                                Span.dummySpan())),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitHeader() throws IOException {
        YokohamaUnitParser.HeaderContext ctx = parser("|a|b|\n").header();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Ident> actual = instance.visitHeader(ctx);
        List<Ident> expected = Arrays.asList(new Ident("a", Span.dummySpan()), new Ident("b", Span.dummySpan()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitRows() throws IOException {
        YokohamaUnitParser.RowsContext ctx = parser("|`a`|`b`|\n|`c`|`d`|\n").rows();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Row> actual = instance.visitRows(ctx);
        List<Row> expected = Arrays.asList(
                new Row(
                        Arrays.asList(
                                new ExprCell(new QuotedExpr("a", Span.dummySpan()), Span.dummySpan()),
                                new ExprCell(new QuotedExpr("b", Span.dummySpan()), Span.dummySpan())),
                        Span.dummySpan()),
                new Row(
                        Arrays.asList(
                                new ExprCell(new QuotedExpr("c", Span.dummySpan()), Span.dummySpan()),
                                new ExprCell(new QuotedExpr("d", Span.dummySpan()), Span.dummySpan())),
                        Span.dummySpan()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitRow() throws IOException {
        YokohamaUnitParser.RowContext ctx = parser("|`a`|`b`|\n").row();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Row actual = instance.visitRow(ctx);
        Row expected =
                new Row(
                        Arrays.asList(
                                new ExprCell(new QuotedExpr("a", Span.dummySpan()), Span.dummySpan()),
                                new ExprCell(new QuotedExpr("b", Span.dummySpan()), Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitFourPhaseTest() throws IOException {
        YokohamaUnitParser.FourPhaseTestContext ctx = parser("# Test: Four phase test\n## Verify\nAssert `x` is `1`.").fourPhaseTest();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        FourPhaseTest actual = instance.visitFourPhaseTest(ctx);
        FourPhaseTest expected = new FourPhaseTest(
                "Four phase test",
                Optional.empty(),
                Optional.empty(),
                new VerifyPhase(
                        Optional.empty(),
                        Arrays.asList(
                                new Assertion(
                                        Arrays.asList(
                                                new Proposition(
                                                        new QuotedExpr("x", Span.dummySpan()),
                                                        new IsPredicate(
                                                                new EqualToMatcher(
                                                                        new QuotedExpr("1", Span.dummySpan()),
                                                                        Span.dummySpan()),
                                                                Span.dummySpan()),
                                                        Span.dummySpan())),
                                        Fixture.none(),
                                        Span.dummySpan())),
                        Span.dummySpan()
                ),
                Optional.empty(),
                Span.dummySpan()
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitFourPhaseTest2() throws IOException {
        YokohamaUnitParser.FourPhaseTestContext ctx = parser("# Test: Four phase test\n## Setup\nLet x be `1`.\n## Exercise\nDo `that`.\n## Verify: verification\nAssert that `x` is `1`.\n## Teardown: do that\nDo `this`. Do `that`. ").fourPhaseTest();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        FourPhaseTest actual = instance.visitFourPhaseTest(ctx);
        FourPhaseTest expected = new FourPhaseTest(
                "Four phase test",
                Optional.of(new Phase(
                        Optional.empty(),
                        Arrays.asList(new LetStatement(
                                Arrays.asList(
                                        new SingleBinding(
                                                new Ident("x", Span.dummySpan()),
                                                new QuotedExpr("1", Span.dummySpan()),
                                                Span.dummySpan())),
                                Span.dummySpan())),
                        Arrays.asList(),
                        Span.dummySpan())),
                Optional.of(new Phase(
                        Optional.empty(),
                        Collections.emptyList(),
                        Arrays.asList(
                                new Execution(
                                        Arrays.asList(
                                                new QuotedExpr("that", Span.dummySpan())),
                                        Span.dummySpan())),
                        Span.dummySpan())),
                new VerifyPhase(
                        Optional.of("verification"),
                        Arrays.asList(
                                new Assertion(
                                        Arrays.asList(
                                                new Proposition(
                                                        new QuotedExpr("x", Span.dummySpan()),
                                                        new IsPredicate(
                                                                new EqualToMatcher(
                                                                        new QuotedExpr("1", Span.dummySpan()),
                                                                        Span.dummySpan()),
                                                                Span.dummySpan()),
                                                        Span.dummySpan())),
                                Fixture.none(),
                                Span.dummySpan())),
                        Span.dummySpan()),
                Optional.of(new Phase(
                        Optional.of("do that"),
                        Collections.emptyList(),
                        Arrays.asList(
                                new Execution(
                                        Arrays.asList(
                                                new QuotedExpr("this", Span.dummySpan())),
                                        Span.dummySpan()),
                                new Execution(
                                        Arrays.asList(
                                                new QuotedExpr("that", Span.dummySpan())),
                                        Span.dummySpan())
                        ),
                        Span.dummySpan())
                ),
                Span.dummySpan()

        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("# Setup\nLet x be `1`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                Optional.empty(),
                Arrays.asList(new LetStatement(
                        Arrays.asList(
                                new SingleBinding(
                                        new Ident("x", Span.dummySpan()),
                                        new QuotedExpr("1", Span.dummySpan()),
                                        Span.dummySpan())),
                        Span.dummySpan())),
                Arrays.asList(),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup2() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("## Setup: x = 1\nDo `this`. Do `that`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                Optional.of("x = 1"),
                Collections.emptyList(),
                Arrays.asList(
                        new Execution(Arrays.asList(new QuotedExpr("this", Span.dummySpan())), Span.dummySpan()),
                        new Execution(Arrays.asList(new QuotedExpr("that", Span.dummySpan())), Span.dummySpan())),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup3() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("# Setup\nLet x = `1`.\nDo `that`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                Optional.empty(),
                Arrays.asList(new LetStatement(
                        Arrays.asList(
                                new SingleBinding(
                                        new Ident("x", Span.dummySpan()),
                                        new QuotedExpr("1", Span.dummySpan()),
                                        Span.dummySpan())),
                        Span.dummySpan())),
                Arrays.asList(
                        new Execution(
                                Arrays.asList(
                                        new QuotedExpr("that", Span.dummySpan())),
                                Span.dummySpan())),
                Span.dummySpan()
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitExercise() throws IOException {
        YokohamaUnitParser.ExerciseContext ctx = parser("# Exercise: x = 1\nDo `this`. Do `that`.").exercise();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitExercise(ctx);
        Phase expected = new Phase(
                Optional.of("x = 1"),
                Collections.emptyList(),
                Arrays.asList(
                        new Execution(
                                Arrays.asList(
                                        new QuotedExpr("this", Span.dummySpan())),
                                Span.dummySpan()),
                        new Execution(
                                Arrays.asList(
                                        new QuotedExpr("that", Span.dummySpan())),
                                Span.dummySpan())
                ),
                Span.dummySpan()
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitVerify() throws IOException {
        YokohamaUnitParser.VerifyContext ctx = parser("# Verify\nAssert that `x` is `1`.").verify();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase expResult = null;
        VerifyPhase actual = instance.visitVerify(ctx);
        VerifyPhase expected = new VerifyPhase(
                Optional.empty(),
                Arrays.asList(
                        new Assertion(
                                Arrays.asList(
                                        new Proposition(
                                                new QuotedExpr("x", Span.dummySpan()),
                                                new IsPredicate(
                                                        new EqualToMatcher(
                                                                new QuotedExpr("1", Span.dummySpan()),
                                                                Span.dummySpan()),
                                                        Span.dummySpan()),
                                                Span.dummySpan())),
                                Fixture.none(),
                                Span.dummySpan())),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTeardown() throws IOException {
        YokohamaUnitParser.TeardownContext ctx = parser("## Teardown: do that\nDo `this`. Do `that`.").teardown();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitTeardown(ctx);
        Phase expected = new Phase(
                Optional.of("do that"),
                Collections.emptyList(),
                Arrays.asList(
                        new Execution(
                                Arrays.asList(
                                        new QuotedExpr("this", Span.dummySpan())),
                                Span.dummySpan()),
                        new Execution(
                                Arrays.asList(
                                        new QuotedExpr("that", Span.dummySpan())),
                                Span.dummySpan())),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTeardown2() throws IOException {
        YokohamaUnitParser.TeardownContext ctx = parser("# Teardown\nDo `this`.").teardown();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitTeardown(ctx);
        Phase expected = new Phase(
                Optional.empty(),
                Collections.emptyList(),
                Arrays.asList(
                        new Execution(
                                Arrays.asList(
                                        new QuotedExpr("this", Span.dummySpan())),
                                Span.dummySpan())),
                Span.dummySpan()
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitLetBindings() throws IOException {
        YokohamaUnitParser.LetStatementContext ctx = parser("Let x be `1` and y = `2`.").letStatement();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        LetStatement actual = instance.visitLetStatement(ctx);
        LetStatement expected =
                new LetStatement(
                        Arrays.asList(
                                new SingleBinding(
                                        new Ident("x", Span.dummySpan()),
                                        new QuotedExpr("1", Span.dummySpan()),
                                        Span.dummySpan()),
                                new SingleBinding(
                                        new Ident("y", Span.dummySpan()),
                                        new QuotedExpr("2", Span.dummySpan()),
                                        Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitLetBinding() throws IOException {
        YokohamaUnitParser.LetBindingContext ctx = parser("x = `1`").letBinding();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Binding actual = instance.visitLetBinding(ctx);
        Binding expected =
                new SingleBinding(
                        new Ident("x", Span.dummySpan()),
                        new QuotedExpr("1", Span.dummySpan()),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitExecution() throws IOException {
        YokohamaUnitParser.ExecutionContext ctx = parser("Do `System.out.println(\"test\")`.").execution();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Execution actual = instance.visitExecution(ctx);
        Execution expected =
                new Execution(
                        Arrays.asList(
                                new QuotedExpr("System.out.println(\"test\")", Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitExecution2() throws IOException {
        YokohamaUnitParser.ExecutionContext ctx = parser("Do `this` and `that`.").execution();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Execution actual = instance.visitExecution(ctx);
        Execution expected =
                new Execution(
                        Arrays.asList(
                                new QuotedExpr("this", Span.dummySpan()),
                                new QuotedExpr("that", Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitInvokeExpr() throws IOException {
        YokohamaUnitParser.InvokeExprContext ctx =
                parser("an invocation of `java.lang.String.endsWith(String)` on `s` with \"end\"").invokeExpr();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        InvocationExpr actual = instance.visitInvokeExpr(ctx);
        InvocationExpr expected =
                new InvocationExpr(
                        new ClassType("java.lang.String", Span.dummySpan()),
                        new MethodPattern(
                                "endsWith",
                                Arrays.asList(
                                        new Type(
                                                new ClassType("String", Span.dummySpan()),
                                                0,
                                                Span.dummySpan())
                                ),
                                false,
                                Span.dummySpan()),
                        Optional.of(new QuotedExpr("s", Span.dummySpan())),
                        Arrays.asList(new StringExpr("end", Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitInvokeExpr2() throws IOException {
        YokohamaUnitParser.InvokeExprContext ctx =
                parser("an invocation of `String.isEmpty()` on `s`").invokeExpr();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        InvocationExpr actual = instance.visitInvokeExpr(ctx);
        InvocationExpr expected =
                new InvocationExpr(
                        new ClassType("String", Span.dummySpan()),
                        new MethodPattern(
                                "isEmpty",
                                Arrays.asList(),
                                false,
                                Span.dummySpan()),
                        Optional.of(new QuotedExpr("s", Span.dummySpan())),
                        Arrays.asList(),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitInvokeExpr3() throws IOException {
        YokohamaUnitParser.InvokeExprContext ctx =
                parser("an invocation of `String.valueOf(boolean)` with false").invokeExpr();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        InvocationExpr actual = instance.visitInvokeExpr(ctx);
        InvocationExpr expected =
                new InvocationExpr(
                        new ClassType("String", Span.dummySpan()),
                        new MethodPattern(
                                "valueOf",
                                Arrays.asList(
                                        new Type(
                                                new PrimitiveType(
                                                        Kind.BOOLEAN,
                                                        Span.dummySpan()),
                                                0,
                                                Span.dummySpan())
                                ),
                                false,
                                Span.dummySpan()),
                        Optional.empty(),
                        Arrays.asList(new BooleanExpr(false, Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }
}
