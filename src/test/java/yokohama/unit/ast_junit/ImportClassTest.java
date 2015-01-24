package yokohama.unit.ast_junit;

import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.util.SBuilder;

public class ImportClassTest {
    
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        ImportClass instance = new ImportClass("java.lang.Object");
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("import java.lang.Object;");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
