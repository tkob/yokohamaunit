package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;
import yokohama.unit.util.SBuilder;

public class ClassDeclTest {
    
    @Test
    public void testImportedNames() {
        ClassDecl instance = new ClassDecl("TestClass", Arrays.asList());
        Set<ImportedName> expected = new TreeSet<ImportedName>();
        Set<ImportedName> actual = instance.importedNames(new OgnlExpressionStrategy());
        assertThat(actual, is(expected));
    }

    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        ClassDecl instance = new ClassDecl("TestClass", Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("public class TestClass {");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
