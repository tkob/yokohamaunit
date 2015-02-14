package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableExtractVisitor {
    public List<Table> extractTables(Group group) {
        List<Definition> definitions = group.getDefinitions();
        return definitions
                .stream()
                .flatMap(definition ->
                        definition.accept(
                                test -> Stream.empty(),
                                fourPhaseTest -> Stream.empty(),
                                table -> Stream.of(table)))
                .collect(Collectors.toList());
    }
}
