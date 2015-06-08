package yokohama.unit.ast;

import java.util.Arrays;
import java.util.List;
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
}
