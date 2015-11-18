package yokohama.unit.ast;

public interface DefinitionVisitor<T> {
    T visitTest(Test test);
    T visitFourPhaseTest(FourPhaseTest fourPhasetest);
    T visitTable(Table table);
    T visitCodeBlock(CodeBlock codeBlock);
    T visitHeading(Heading heading);
}
