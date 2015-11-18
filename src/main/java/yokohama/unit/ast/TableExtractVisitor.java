package yokohama.unit.ast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.util.Lists;
import yokohama.unit.util.Pair;

public class TableExtractVisitor {
    public Map<String, Table> extractMap(Group group) {
        List<Table> tables = extractTables(group);
        return Lists.listToMap(
                tables, table -> Pair.of(table.getName(), table));
    }

    public List<Table> extractTables(Group group) {
        List<Definition> definitions = group.getDefinitions();
        return definitions
                .stream()
                .flatMap(definition ->
                        definition.accept(
                                test -> Stream.empty(),
                                fourPhaseTest -> Stream.empty(),
                                table -> Stream.of(table),
                                codeBlock -> Stream.empty(),
                                heading -> Stream.empty()))
                .collect(Collectors.toList());
    }
}
