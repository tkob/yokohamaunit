package yokohama.unit.ast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaslang.Tuple;
import yokohama.unit.util.Lists;

public class CodeBlockExtractVisitor {
    public Map<String, CodeBlock> extractMap(Group group) {
        List<CodeBlock> codeBlocks = visit(group);
        return Lists.listToMap(
                codeBlocks,
                codeBlock ->
                        Tuple.of(
                                codeBlock.getHeading().getLine(), codeBlock));
    }

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
