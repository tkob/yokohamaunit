package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeBlockExtractVisitor {
    public List<CodeBlock> visit(Group group) {
        List<Definition> definitions = group.getDefinitions();
        return definitions
                .stream()
                .flatMap(definition ->
                        definition.accept(
                                test -> Stream.empty(),
                                fourPhaseTest -> Stream.empty(),
                                table -> Stream.empty(),
                                codeBlock -> Stream.of(codeBlock),
                                heading -> Stream.empty()))
                .collect(Collectors.toList());
    }
}
