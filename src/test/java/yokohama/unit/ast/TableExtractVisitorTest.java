package yokohama.unit.ast;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TableExtractVisitorTest {
    @Test
    public void testExtractTables() {
        List<Definition> definitions = Arrays.asList();
        Group group = new Group(definitions, yokohama.unit.ast.Span.dummySpan());
        TableExtractVisitor instance = new TableExtractVisitor();
        List<Table> result = instance.extractTables(group);
        assertThat(result.size(), is(0));
    }

    @Test
    public void testExtractTables1() {
        List<Definition> definitions = Arrays.asList(
                new Table("table 1", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan()),
                new yokohama.unit.ast.Test("test name", Arrays.asList(), 0, yokohama.unit.ast.Span.dummySpan()),
                new Table("table 2", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan())
        );
        Group group = new Group(definitions, yokohama.unit.ast.Span.dummySpan());
        TableExtractVisitor instance = new TableExtractVisitor();
        List<Table> actual = instance.extractTables(group);
        List<Table> expected = Arrays.asList(
                new Table("table 1", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan()),
                new Table("table 2", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan())
        );
        assertThat(actual, is(expected));
    }
}
