package yokohama.unit.ast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import yokohama.unit.util.Lists;
import yokohama.unit.util.Pair;

@RequiredArgsConstructor
public class ChoiceCollectVisitor extends 
        StreamVisitorTemplate<Pair<List<Ident>, List<List<Expr>>>> {
    final List<Table> tables;

    @Override
    public Stream<Pair<List<Ident>, List<List<Expr>>>> visitChoiceBinding(ChoiceBinding choiceBinding) {
        List<Ident> idents = Arrays.asList(choiceBinding.getName());
        List<List<Expr>> ess = Lists.map(
                choiceBinding.getChoices(),
                choice -> Arrays.asList(choice));
        return Stream.of(new Pair<>(idents, ess));
    }

    @Override
    public Stream<Pair<List<Ident>, List<List<Expr>>>> visitTableBinding(
            TableBinding tableBinding) {
        List<Ident> idents = tableBinding.getIdents();
        String name = tableBinding.getName();
        List<List<Expr>> ess;
        switch (tableBinding.getType()) {
            case INLINE:
                Optional<Table> tableOpt =
                        tables.stream()
                                .filter(table -> table.getName().equals(name))
                                .findFirst();
                if (tableOpt.isPresent()) {
                    Table table = tableOpt.get();
                    List<Ident> header = table.getHeader();
                    ess = table.getRows().stream()
                            .map(row ->
                                    Pair.zip(header, row.getCells())
                                            .stream()
                                            .collect(
                                                    () -> new HashMap<Ident, Cell>(),
                                                    (m, p) -> m.put(
                                                            p.getFirst(),
                                                            p.getSecond()),
                                                    (m1, m2) -> m1.putAll(m2)))
                            .map(m -> {
                                return header.stream()
                                        .map(ident -> m.get(ident))
                                        .map(cell -> {
                                            return cell.accept(
                                                    expr -> expr.getExpr(),
                                                    pred -> {
                                                        throw new AstException(
                                                                "invalid reference to predicate cell",
                                                                tableBinding.getSpan());
                                                    });
                                        })
                                        .collect(Collectors.toList());
                            })
                            .collect(Collectors.toList());
                } else {
                    throw new AstException(
                            "no table named '" + name + "' not found",
                            tableBinding.getSpan());
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return Stream.of(new Pair<>(idents, ess));
    }
}
