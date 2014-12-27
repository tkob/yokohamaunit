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
import org.antlr.v4.runtime.tree.TerminalNode;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Binding;
import yokohama.unit.ast.Bindings;
import yokohama.unit.ast.Copula;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.Expr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.LetBinding;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.TableType;
import yokohama.unit.ast.VerifyPhase;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;

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
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Group result = instance.visitGroup(ctx);
        assertThat(result.getDefinitions().size(), is(1));
    }

    @Test
    public void testVisitDefinition() throws IOException {
        YokohamaUnitParser.DefinitionContext ctx = parser("Test: test name\n Assert `a` is `b`.").definition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Definition result = instance.visitDefinition(ctx);
        assertThat(result, is(instanceOf(yokohama.unit.ast.Test.class)));
    }

    @Test
    public void testVisitDefinition2() throws IOException {
        YokohamaUnitParser.DefinitionContext ctx = parser("Table: table name\n|a|b\n----\n|1|2\n\n").definition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Definition result = instance.visitDefinition(ctx);
        assertThat(result, is(instanceOf(Table.class)));
    }

    @Test
    public void testVisitTest() {
        TerminalNode nameTerminalNode = mock(TerminalNode.class);
        YokohamaUnitParser.TestContext ctx = mock(YokohamaUnitParser.TestContext.class);
        when(nameTerminalNode.getText()).thenReturn("test name");
        when(ctx.TestName()).thenReturn(nameTerminalNode);
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        yokohama.unit.ast.Test result = instance.visitTest(ctx);
        assertEquals("test name", result.getName());
    }

    @Test
    public void testVisitHash() throws IOException {
        YokohamaUnitParser.HashContext ctx = parser("#").hash();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Integer actual = instance.visitHash(ctx);
        Integer expected = 1;
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitHash2() throws IOException {
        YokohamaUnitParser.HashContext ctx = parser("######").hash();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Integer actual = instance.visitHash(ctx);
        Integer expected = 6;
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitAssertion() throws IOException {
        YokohamaUnitParser.AssertionContext ctx = parser("Assert `a` is `b`.").assertion();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Assertion actual = instance.visitAssertion(ctx);
        Assertion expected = new Assertion(
                Arrays.asList(new Proposition(new Expr("a"), Copula.IS, new Expr("b"))),
                Fixture.none());
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitAssertion2() throws IOException {
        YokohamaUnitParser.AssertionContext ctx = parser("Assert `x` is `y` where x = `1`.").assertion();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Assertion actual = instance.visitAssertion(ctx);
        Assertion expected = new Assertion(
                Arrays.asList(new Proposition(new Expr("x"), Copula.IS, new Expr("y"))),
                new Bindings(Arrays.asList(new Binding("x", new Expr("1")))));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitCopula() {
        YokohamaUnitParser.CopulaContext ctx = mock(YokohamaUnitParser.CopulaContext.class);
        when(ctx.getText()).thenReturn("is");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Copula expResult = Copula.IS;
        Copula result = instance.visitCopula(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitCopula2() {
        YokohamaUnitParser.CopulaContext ctx = mock(YokohamaUnitParser.CopulaContext.class);
        when(ctx.getText()).thenReturn("throws");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Copula expResult = Copula.THROWS;
        Copula result = instance.visitCopula(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitCopula3() {
        YokohamaUnitParser.CopulaContext ctx = mock(YokohamaUnitParser.CopulaContext.class);
        when(ctx.getText()).thenReturn("isnot");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Copula expResult = Copula.IS_NOT;
        Copula result = instance.visitCopula(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitBindings() throws IOException {
        YokohamaUnitParser.BindingsContext ctx = parser("where x = `1`").bindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Bindings result = instance.visitBindings(ctx);
        assertThat(result.getBindings().size(), is(1));
    }

    @Test
    public void testVisitBindings2() throws IOException {
        YokohamaUnitParser.BindingsContext ctx = parser("where x = `1` and y = `2`").bindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Bindings result = instance.visitBindings(ctx);
        assertThat(result.getBindings().size(), is(2));
    }

    @Test
    public void testVisitBinding() throws IOException {
        YokohamaUnitParser.BindingContext ctx = parser("x = `1`").binding();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Binding result = instance.visitBinding(ctx);
        assertThat(result, is(new Binding("x", new Expr("1"))));
    }

    @Test
    public void testVisitCondition() throws IOException {
        YokohamaUnitParser.ConditionContext ctx = parser("for all rules in Table \"table 1\"").condition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Fixture result = instance.visitCondition(ctx);
        assertThat(result, is(instanceOf(TableRef.class)));
    }

    @Test
    public void testVisitCondition2() throws IOException {
        YokohamaUnitParser.ConditionContext ctx = parser("where x = `1`").condition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Fixture result = instance.visitCondition(ctx);
        assertThat(result, is(instanceOf(Bindings.class)));
    }

    @Test
    public void testVisitTableRef() throws IOException {
        YokohamaUnitParser.TableRefContext ctx = parser("for all rules in Table \"table name\"").tableRef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        //Object expResult = null;
        TableRef result = instance.visitTableRef(ctx);
        assertEquals("table name", result.getName());
    }

    @Test
    public void testVisitTableType() {
        YokohamaUnitParser.TableTypeContext ctx = mock(YokohamaUnitParser.TableTypeContext.class);
        when(ctx.getText()).thenReturn("Table");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Object expResult = TableType.INLINE;
        Object result = instance.visitTableType(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitTableType2() {
        YokohamaUnitParser.TableTypeContext ctx = mock(YokohamaUnitParser.TableTypeContext.class);
        when(ctx.getText()).thenReturn("CSV");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Object expResult = TableType.CSV;
        Object result = instance.visitTableType(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitTableType3() {
        YokohamaUnitParser.TableTypeContext ctx = mock(YokohamaUnitParser.TableTypeContext.class);
        when(ctx.getText()).thenReturn("TSV");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Object expResult = TableType.TSV;
        Object result = instance.visitTableType(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitTableType4() {
        YokohamaUnitParser.TableTypeContext ctx = mock(YokohamaUnitParser.TableTypeContext.class);
        when(ctx.getText()).thenReturn("Excel");
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Object expResult = TableType.EXCEL;
        Object result = instance.visitTableType(ctx);
        assertEquals(expResult, result);
    }

    @Test
    public void testVisitPropositions() throws IOException {
        YokohamaUnitParser.PropositionsContext ctx = parser("`a` is `b`").propositions();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        List<Proposition> result = instance.visitPropositions(ctx);
        assertThat(result.size(), is(1));
    }

    @Test
    public void testVisitPropositions2() throws IOException {
        YokohamaUnitParser.PropositionsContext ctx = parser("`a` is `b` and `c` is `d`").propositions();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        List<Proposition> result = instance.visitPropositions(ctx);
        assertThat(result.size(), is(2));
    }

    @Test
    public void testVisitProposition() throws IOException {
        YokohamaUnitParser.PropositionContext ctx = parser("`a` is `b`").proposition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Proposition actual = instance.visitProposition(ctx);
        Proposition expected = new Proposition(new Expr("a"), Copula.IS, new Expr("b"));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTableDef() throws IOException {
        YokohamaUnitParser.TableDefContext ctx = parser("Table: table name\n|a|b\n|1|2\n").tableDef();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Table actual = instance.visitTableDef(ctx);
        Table expected = new Table(
                "table name",
                Arrays.asList("a", "b"),
                Arrays.asList(new Row(
                        Arrays.asList(new Expr("1"), new Expr("2")))));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitHeader() throws IOException {
        YokohamaUnitParser.HeaderContext ctx = parser("|a|b\n").header();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        List<String> actual = instance.visitHeader(ctx);
        List<String> expected = Arrays.asList("a", "b");
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitRows() throws IOException {
        YokohamaUnitParser.RowsContext ctx = parser("|a|b|\n|c|d|\n", YokohamaUnitLexer.IN_TABLE_ONSET).rows();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        List<Row> actual = instance.visitRows(ctx);
        List<Row> expected = Arrays.asList(
                new Row(Arrays.asList(new Expr("a"), new Expr("b"))),
                new Row(Arrays.asList(new Expr("c"), new Expr("d"))));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitRow() throws IOException {
        YokohamaUnitParser.RowContext ctx = parser("|a|b\n", YokohamaUnitLexer.IN_TABLE_ONSET).row();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Row actual = instance.visitRow(ctx);
        Row expected = new Row(Arrays.asList(new Expr("a"), new Expr("b")));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitFourPhaseTest() throws IOException {
        YokohamaUnitParser.FourPhaseTestContext ctx = parser("Test: Four phase test\nVerify\nAssert `x` is `1`.").fourPhaseTest();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
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
                                                new Proposition(new Expr("x"), Copula.IS, new Expr("1"))), Fixture.none()))
                ),
                Optional.empty()
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitFourPhaseTest2() throws IOException {
        YokohamaUnitParser.FourPhaseTestContext ctx = parser("# Test: Four phase test\n## Setup\nLet x be `1`.\n## Exercise\nDo `that`.\n## Verify: verification\nAssert that `x` is `1`.\n## Teardown: do that\nDo `this`. Do `that`. ").fourPhaseTest();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        FourPhaseTest actual = instance.visitFourPhaseTest(ctx);
        FourPhaseTest expected = new FourPhaseTest(
                1,
                "Four phase test",
                Optional.of(new Phase(
                        2,
                        Optional.empty(),
                        Optional.of(new LetBindings(Arrays.asList(new LetBinding("x", new Expr("1"))))),
                        Arrays.asList())
                ),
                Optional.of(new Phase(
                        2,
                        Optional.empty(),
                        Optional.empty(),
                        Arrays.asList(new Execution(Arrays.asList(new Expr("that")))))
                ),
                new VerifyPhase(
                        2,
                        Optional.of("verification"),
                        Arrays.asList(
                                new Assertion(
                                        Arrays.asList(
                                                new Proposition(new Expr("x"), Copula.IS, new Expr("1"))), Fixture.none()))
                ),
                Optional.of(new Phase(
                        2,
                        Optional.of("do that"),
                        Optional.empty(),
                        Arrays.asList(
                                new Execution(Arrays.asList(new Expr("this"))),
                                new Execution(Arrays.asList(new Expr("that")))
                        ))
                )
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("Setup\nLet x be `1`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                0,
                Optional.empty(),
                Optional.of(new LetBindings(Arrays.asList(new LetBinding("x", new Expr("1"))))),
                Arrays.asList()
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup2() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("## Setup: x = 1\nDo `this`. Do `that`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                2,
                Optional.of("x = 1"),
                Optional.empty(),
                Arrays.asList(
                        new Execution(Arrays.asList(new Expr("this"))),
                        new Execution(Arrays.asList(new Expr("that")))
                )
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitSetup3() throws IOException {
        YokohamaUnitParser.SetupContext ctx = parser("Setup\nLet x = `1`\nDo `that`.").setup();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase actual = instance.visitSetup(ctx);
        Phase expected = new Phase(
                0,
                Optional.empty(),
                Optional.of(new LetBindings(Arrays.asList(new LetBinding("x", new Expr("1"))))),
                Arrays.asList(new Execution(Arrays.asList(new Expr("that"))))
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitExercise() throws IOException {
        YokohamaUnitParser.ExerciseContext ctx = parser("Exercise: x = 1\nDo `this`. Do `that`.").exercise();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase actual = instance.visitExercise(ctx);
        Phase expected = new Phase(
                0,
                Optional.of("x = 1"),
                Optional.empty(),
                Arrays.asList(
                        new Execution(Arrays.asList(new Expr("this"))),
                        new Execution(Arrays.asList(new Expr("that")))
                )
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitVerify() throws IOException {
        YokohamaUnitParser.VerifyContext ctx = parser("Verify\nAssert that `x` is `1`.").verify();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase expResult = null;
        VerifyPhase actual = instance.visitVerify(ctx);
        VerifyPhase expected = new VerifyPhase(
                0,
                Optional.empty(),
                Arrays.asList(
                        new Assertion(
                                Arrays.asList(new Proposition(new Expr("x"), Copula.IS, new Expr("1"))),
                                Fixture.none()))
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTeardown() throws IOException {
        YokohamaUnitParser.TeardownContext ctx = parser("## Teardown: do that\nDo `this`. Do `that`.").teardown();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase actual = instance.visitTeardown(ctx);
        Phase expected = new Phase(
                2,
                Optional.of("do that"),
                Optional.empty(),
                Arrays.asList(
                        new Execution(Arrays.asList(new Expr("this"))),
                        new Execution(Arrays.asList(new Expr("that")))
                )
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitTeardown2() throws IOException {
        YokohamaUnitParser.TeardownContext ctx = parser("Teardown\nDo `this`.").teardown();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Phase actual = instance.visitTeardown(ctx);
        Phase expected = new Phase(
                0,
                Optional.empty(),
                Optional.empty(),
                Arrays.asList(
                        new Execution(Arrays.asList(new Expr("this")))
                )
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitLetBindings() throws IOException {
        YokohamaUnitParser.LetBindingsContext ctx = parser("Let x be `1` and y = `2`.").letBindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        LetBindings actual = instance.visitLetBindings(ctx);
        LetBindings expected = new LetBindings(Arrays.asList(
                new LetBinding("x", new Expr("1")),
                new LetBinding("y", new Expr("2"))
                ));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitLetBinding() throws IOException {
        YokohamaUnitParser.LetBindingContext ctx = parser("x = `1`").letBinding();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        LetBinding actual = instance.visitLetBinding(ctx);
        LetBinding expected = new LetBinding("x", new Expr("1"));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitExecution() throws IOException {
        YokohamaUnitParser.ExecutionContext ctx = parser("Do `System.out.println(\"test\")`.").execution();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Execution actual = instance.visitExecution(ctx);
        Execution expected = new Execution(Arrays.asList(new Expr("System.out.println(\"test\")")));
        assertThat(actual, is(expected));
    }

    @Test
    public void testVisitExecution2() throws IOException {
        YokohamaUnitParser.ExecutionContext ctx = parser("Do `this` and `that`.").execution();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Execution actual = instance.visitExecution(ctx);
        Execution expected = new Execution(Arrays.asList(new Expr("this"), new Expr("that")));
        assertThat(actual, is(expected));
    }
}
