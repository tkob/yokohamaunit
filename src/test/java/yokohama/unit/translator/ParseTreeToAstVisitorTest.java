package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.LetBinding;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Span;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.TableType;
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
        YokohamaUnitParser.GroupContext ctx = parser("Test: test name\nAssert `a` is `b`.").group();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Group result = instance.visitGroup(ctx);
        assertThat(result.getDefinitions().size(), is(1));
    }

    @Test
    public void testVisitDefinition() throws IOException {
        YokohamaUnitParser.DefinitionContext ctx = parser("Test: test name\n Assert `a` is `b`.").definition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Definition result = instance.visitDefinition(ctx);
        assertThat(result, is(instanceOf(yokohama.unit.ast.Test.class)));
    }

    @Test
    public void testVisitDefinition2() throws IOException {
        YokohamaUnitParser.DefinitionContext ctx = parser("[table name]\n|a|b\n----\n|1|2\n\n").definition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Definition result = instance.visitDefinition(ctx);
        assertThat(result, is(instanceOf(Table.class)));
    }

    @Test
    public void testVisitTest() throws IOException {
        YokohamaUnitParser.TestContext ctx = parser("Test: test name\nAssert `1` is `1`.").test();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        yokohama.unit.ast.Test result = instance.visitTest(ctx);
        assertEquals("test name", result.getName());
    }

    @Test
    public void testVisitHash() throws IOException {
        YokohamaUnitParser.HashContext ctx = parser("#").hash();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Integer actual = instance.visitHash(ctx);
        Integer expected = 1;
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitHash2() throws IOException {
        YokohamaUnitParser.HashContext ctx = parser("######").hash();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Integer actual = instance.visitHash(ctx);
        Integer expected = 6;
        assertThat(actual, is(expected));
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
                                new Binding(
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
        Binding expected = new Binding(
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
    public void testVisitPropositions() throws IOException {
        YokohamaUnitParser.PropositionsContext ctx = parser("`a` is `b`").propositions();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Proposition> result = instance.visitPropositions(ctx);
        assertThat(result.size(), is(1));
    }

    @Test
    public void testVisitPropositions2() throws IOException {
        YokohamaUnitParser.PropositionsContext ctx = parser("`a` is `b` and `c` is `d`").propositions();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Proposition> result = instance.visitPropositions(ctx);
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
        YokohamaUnitParser.TableDefContext ctx = parser("[table name]\n|a|b\n|1|2\n").tableDef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Table actual = instance.visitTableDef(ctx);
        Table expected = new Table(
                "table name",
                Arrays.asList(new Ident("a", Span.dummySpan()), new Ident("b", Span.dummySpan())),
                Arrays.asList(
                        new Row(
                                Arrays.asList(
                                        new QuotedExpr("1", Span.dummySpan()),
                                        new QuotedExpr("2", Span.dummySpan())),
                                Span.dummySpan())),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitHeader() throws IOException {
        YokohamaUnitParser.HeaderContext ctx = parser("|a|b\n").header();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Ident> actual = instance.visitHeader(ctx);
        List<Ident> expected = Arrays.asList(new Ident("a", Span.dummySpan()), new Ident("b", Span.dummySpan()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitRows() throws IOException {
        YokohamaUnitParser.RowsContext ctx = parser("|a|b|\n|c|d|\n", YokohamaUnitLexer.IN_TABLE_ONSET).rows();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        List<Row> actual = instance.visitRows(ctx);
        List<Row> expected = Arrays.asList(
                new Row(
                        Arrays.asList(
                                new QuotedExpr("a", Span.dummySpan()),
                                new QuotedExpr("b", Span.dummySpan())),
                        Span.dummySpan()),
                new Row(
                        Arrays.asList(
                                new QuotedExpr("c", Span.dummySpan()),
                                new QuotedExpr("d", Span.dummySpan())),
                        Span.dummySpan()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitRow() throws IOException {
        YokohamaUnitParser.RowContext ctx = parser("|a|b\n", YokohamaUnitLexer.IN_TABLE_ONSET).row();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Row actual = instance.visitRow(ctx);
        Row expected =
                new Row(
                        Arrays.asList(
                                new QuotedExpr("a", Span.dummySpan()),
                                new QuotedExpr("b", Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitFourPhaseTest() throws IOException {
        YokohamaUnitParser.FourPhaseTestContext ctx = parser("Test: Four phase test\nVerify\nAssert `x` is `1`.").fourPhaseTest();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        FourPhaseTest actual = instance.visitFourPhaseTest(ctx);
        FourPhaseTest expected = new FourPhaseTest(
                0,
                "Four phase test",
                Optional.empty(),
                Optional.empty(),
                new VerifyPhase(
                        0,
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
                1,
                "Four phase test",
                Optional.of(new Phase(
                        2,
                        Optional.empty(),
                        Optional.of(
                                new LetBindings(
                                        Arrays.asList(
                                                new LetBinding(
                                                        "x",
                                                        new QuotedExpr("1", Span.dummySpan()),
                                                        Span.dummySpan())),
                                        Span.dummySpan())),
                        Arrays.asList(),
                        Span.dummySpan())),
                Optional.of(new Phase(
                        2,
                        Optional.empty(),
                        Optional.empty(),
                        Arrays.asList(
                                new Execution(
                                        Arrays.asList(
                                                new QuotedExpr("that", Span.dummySpan())),
                                        Span.dummySpan())),
                        Span.dummySpan())),
                new VerifyPhase(
                        2,
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
                        2,
                        Optional.of("do that"),
                        Optional.empty(),
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
        YokohamaUnitParser.SetupContext ctx = parser("Setup\nLet x be `1`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                0,
                Optional.empty(),
                Optional.of(
                        new LetBindings(
                                Arrays.asList(
                                        new LetBinding(
                                                "x",
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
                2,
                Optional.of("x = 1"),
                Optional.empty(),
                Arrays.asList(
                        new Execution(Arrays.asList(new QuotedExpr("this", Span.dummySpan())), Span.dummySpan()),
                        new Execution(Arrays.asList(new QuotedExpr("that", Span.dummySpan())), Span.dummySpan())),
                Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup3() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("Setup\nLet x = `1`.\nDo `that`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                0,
                Optional.empty(),
                Optional.of(
                        new LetBindings(
                                Arrays.asList(
                                        new LetBinding(
                                                "x",
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
        YokohamaUnitParser.ExerciseContext ctx = parser("Exercise: x = 1\nDo `this`. Do `that`.").exercise();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitExercise(ctx);
        Phase expected = new Phase(
                0,
                Optional.of("x = 1"),
                Optional.empty(),
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
        YokohamaUnitParser.VerifyContext ctx = parser("Verify\nAssert that `x` is `1`.").verify();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase expResult = null;
        VerifyPhase actual = instance.visitVerify(ctx);
        VerifyPhase expected = new VerifyPhase(
                0,
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
                2,
                Optional.of("do that"),
                Optional.empty(),
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
        YokohamaUnitParser.TeardownContext ctx = parser("Teardown\nDo `this`.").teardown();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        Phase actual = instance.visitTeardown(ctx);
        Phase expected = new Phase(
                0,
                Optional.empty(),
                Optional.empty(),
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
        YokohamaUnitParser.LetBindingsContext ctx = parser("Let x be `1` and y = `2`.").letBindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        LetBindings actual = instance.visitLetBindings(ctx);
        LetBindings expected =
                new LetBindings(
                        Arrays.asList(
                                new LetBinding(
                                        "x",
                                        new QuotedExpr("1", Span.dummySpan()),
                                        Span.dummySpan()),
                                new LetBinding(
                                        "y",
                                        new QuotedExpr("2", Span.dummySpan()),
                                        Span.dummySpan())),
                        Span.dummySpan());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitLetBinding() throws IOException {
        YokohamaUnitParser.LetBindingContext ctx = parser("x = `1`").letBinding();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor(Optional.empty());
        LetBinding actual = instance.visitLetBinding(ctx);
        LetBinding expected =
                new LetBinding(
                        "x",
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
}
