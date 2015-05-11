package yokohama.unit.ast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import yokohama.unit.position.ErrorMessage;

public class AnchorCheckVisitor {
    TableExtractVisitor tableExtractVisitor = new TableExtractVisitor();
    CodeBlockExtractVisitor codeBlockExtractVisitor = new CodeBlockExtractVisitor();

    public List<ErrorMessage> check(Group group) {
        Map<String, Table> tableMap = tableExtractVisitor.extractMap(group);
        Map<String, CodeBlock> codeBlockMap = codeBlockExtractVisitor.extractMap(group);
        return new AuxAnchorCheckVisitor(tableMap, codeBlockMap)
                .visitGroup(group)
                .collect(Collectors.toList());
    }
}

@AllArgsConstructor
class AuxAnchorCheckVisitor extends CheckVisitorTemplate {
    Map<String, Table> tableMap;
    Map<String, CodeBlock> codeBlockMap;

    @Override
    public Stream<ErrorMessage> visitAnchorExpr(AnchorExpr anchorExpr) {
        String anchor = anchorExpr.getAnchor();
        return codeBlockMap.containsKey(anchor) || tableMap.containsKey(anchor)
                ? Stream.empty()
                : Stream.of(
                        new ErrorMessage(
                                "Anchor [" + anchor + "] has no target",
                                anchorExpr.getSpan()));
    }

    @Override
    public Stream<ErrorMessage> visitTableRef(TableRef tableRef) {
        String ref = tableRef.getName();
        TableType tableType = tableRef.getType();
        return Stream.concat(
                tableRef.getIdents().stream().flatMap(this::visitIdent),
                tableMap.containsKey(ref) || tableType != TableType.INLINE
                        ? Stream.empty()
                        : Stream.of(
                                new ErrorMessage(
                                        "Table reference [" + ref + "] has no target",
                                        tableRef.getSpan())));
    }
}