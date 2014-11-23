package yokohama.unit.ast_junit;

import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;
import yokohama.unit.util.SBuilder;

public class ImportStaticTest {
    
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        ImportStatic instance = new ImportStatic("org.junit.Assert.*");
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("import static org.junit.Assert.*;");

        assertThat(actual.toString(), is(expected.toString()));
    }
    
}
