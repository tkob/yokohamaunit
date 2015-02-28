package yokohama.unit.ast_junit;

import java.util.Arrays;
import org.apache.commons.lang3.text.StrBuilder;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CompilationUnitTest {
    @Test
    public void testGetText() {
        String packageName = "yokohama.unit.example";
        ClassDecl classDecl = new ClassDecl("", Arrays.asList());
        CompilationUnit instance = new CompilationUnit(packageName, classDecl);
        String actual = instance.getText(new OgnlExpressionStrategy(), new MockitoMockStrategy());
        StrBuilder sb = new StrBuilder();
        sb.appendln("package yokohama.unit.example;");
        sb.appendNewLine();
        String expected = sb.toString();
        assertTrue(actual.startsWith(expected));
    }
}
