package yokohama.unit.ast;

import java.util.function.Function;

public interface Definition {

    <T> T accept(DefinitionVisitor<T> visitor);

    default <T> T accept(
            Function<Test, T> visitTest_,
            Function<FourPhaseTest, T> visitFourPhaseTest_,
            Function<Table, T> visitTable_,
            Function<CodeBlock, T> visitCodeBlock_,
            Function<Heading, T> visitHeading_
    ) {
        return accept(new DefinitionVisitor<T>() {
            @Override
            public T visitTest(Test test) {
                return visitTest_.apply(test);
            }
            @Override
            public T visitFourPhaseTest(FourPhaseTest fourPhaseTest) {
                return visitFourPhaseTest_.apply(fourPhaseTest);
            }
            @Override
            public T visitTable(Table table) {
                return visitTable_.apply(table);
            }

            @Override
            public T visitCodeBlock(CodeBlock codeBlock) {
                return visitCodeBlock_.apply(codeBlock);
            }

            @Override
            public T visitHeading(Heading heading) {
                return visitHeading_.apply(heading);
            }
        });
    }
}
