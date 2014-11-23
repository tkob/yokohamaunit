package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
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
import yokohama.unit.ast.Expr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.TableType;
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
    public void testVisitDefinition1() throws IOException {
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
    public void testVisitAssertion1() throws IOException {
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
        YokohamaUnitParser.AssertionContext ctx = parser("Assert `a` is `b` where a = `1`.").assertion();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Assertion actual = instance.visitAssertion(ctx);
        Assertion expected = new Assertion(
                Arrays.asList(new Proposition(new Expr("a"), Copula.IS, new Expr("b"))),
                new Bindings(Arrays.asList(new Binding("a", new Expr("1")))));
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
    public void testVisitBindings1() throws IOException {
        YokohamaUnitParser.BindingsContext ctx = parser("where a = `1`").bindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Bindings result = instance.visitBindings(ctx);
        assertThat(result.getBindings().size(), is(1));
    }

    @Test
    public void testVisitBindings2() throws IOException {
        YokohamaUnitParser.BindingsContext ctx = parser("where a = `1` and b = `2`").bindings();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Bindings result = instance.visitBindings(ctx);
        assertThat(result.getBindings().size(), is(2));
    }

    @Test
    public void testVisitBinding() throws IOException {
        YokohamaUnitParser.BindingContext ctx = parser("a = `1`").binding();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Binding result = instance.visitBinding(ctx);
        assertThat(result, is(new Binding("a", new Expr("1"))));
    }

    @Test
    public void testVisitCondition1() throws IOException {
        YokohamaUnitParser.ConditionContext ctx = parser("according to Table \"table 1\"").condition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Fixture result = instance.visitCondition(ctx);
        assertThat(result, is(instanceOf(TableRef.class)));
    }

    @Test
    public void testVisitCondition2() throws IOException {
        YokohamaUnitParser.ConditionContext ctx = parser("where a = `1`").condition();
        ParseTreeToAstVisitor instance = new ParseTreeToAstVisitor();
        Fixture result = instance.visitCondition(ctx);
        assertThat(result, is(instanceOf(Bindings.class)));
    }

    @Test
    public void testVisitTableRef() throws IOException {
        YokohamaUnitParser.TableRefContext ctx = parser("according to Table \"table name\"").tableRef();
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
    public void testVisitPropositions1() throws IOException {
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
}
