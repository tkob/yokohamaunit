package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author tkob
 */
public class CompilationUnitTest {
    @Test
    public void testGetText() {
        String packageName = "yokohama.unit.example";
        ClassDecl classDecl = new ClassDecl("", Arrays.asList());
        CompilationUnit instance = new CompilationUnit(packageName, classDecl);
        String actual = instance.getText(new OgnlExpressionStrategy());
        StrBuilder sb = new StrBuilder();
        sb.appendln("package yokohama.unit.example;");
        sb.appendNewLine();
        String expected = sb.toString();
        assertTrue(actual.startsWith(expected));
    }

    @Test
    public void testGetText1(@Mocked ClassDecl classDecl) {
        String packageName = "yokohama.unit.example";
        ExpressionStrategy expressionStrategy = new OgnlExpressionStrategy();
        Set<ImportedName> importedNames = new TreeSet<>();
        importedNames.add(new ImportClass("yokohama.unit.example.B"));
        importedNames.add(new ImportClass("yokohama.unit.example.G"));
        importedNames.add(new ImportStatic("yokohama.unit.example.C.*"));
        importedNames.add(new ImportStatic("yokohama.unit.example.E.*"));
        importedNames.add(new ImportClass("yokohama.unit.example.D"));
        importedNames.add(new ImportStatic("yokohama.unit.example.F.*"));
        importedNames.add(new ImportClass("yokohama.unit.example.A"));
        new Expectations() {{
            classDecl.importedNames(expressionStrategy); result = importedNames;
        }};
        CompilationUnit instance = new CompilationUnit(packageName, classDecl);
        String actual = instance.getText(expressionStrategy);
        StrBuilder sb = new StrBuilder();
        sb.appendln("package yokohama.unit.example;");
        sb.appendNewLine();
        sb.appendln("import yokohama.unit.example.A;");
        sb.appendln("import yokohama.unit.example.B;");
        sb.appendln("import static yokohama.unit.example.C.*;");
        sb.appendln("import yokohama.unit.example.D;");
        sb.appendln("import static yokohama.unit.example.E.*;");
        sb.appendln("import static yokohama.unit.example.F.*;");
        sb.appendln("import yokohama.unit.example.G;");
        sb.appendNewLine();
        String expected = sb.toString();
        assertThat(actual, is(expected));
    }
}
