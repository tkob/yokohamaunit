package yokohama.unit.ast;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TableExtractVisitorTest {
    @Test
    public void testExtractTables() {
        List<Abbreviation> abbreviations = Arrays.asList();
        List<Definition> definitions = Arrays.asList();
        Group group = new Group(abbreviations, definitions, yokohama.unit.position.Span.dummySpan());
        TableExtractVisitor instance = new TableExtractVisitor();
        List<Table> result = instance.extractTables(group);
        assertThat(result.size(), is(0));
    }

    @Test
    public void testExtractTables1() {
        List<Abbreviation> abbreviations = Arrays.asList();
        List<Definition> definitions = Arrays.asList(
                new Table("table 1", Arrays.asList(new Ident("a", yokohama.unit.position.Span.dummySpan())), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.position.Span.dummySpan())), yokohama.unit.position.Span.dummySpan()),
                new yokohama.unit.ast.Test("test name", Arrays.asList(), yokohama.unit.position.Span.dummySpan()),
                new Table("table 2", Arrays.asList(new Ident("a", yokohama.unit.position.Span.dummySpan())), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.position.Span.dummySpan())), yokohama.unit.position.Span.dummySpan())
        );
        Group group = new Group(abbreviations, definitions, yokohama.unit.position.Span.dummySpan());
        TableExtractVisitor instance = new TableExtractVisitor();
        List<Table> actual = instance.extractTables(group);
        List<Table> expected = Arrays.asList(
                new Table("table 1", Arrays.asList(new Ident("a", yokohama.unit.position.Span.dummySpan())), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.position.Span.dummySpan())), yokohama.unit.position.Span.dummySpan()),
                new Table("table 2", Arrays.asList(new Ident("a", yokohama.unit.position.Span.dummySpan())), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.position.Span.dummySpan())), yokohama.unit.position.Span.dummySpan())
        );
        assertThat(actual, is(expected));
    }
}
