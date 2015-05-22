package yokohama.unit.ast;

import java.util.stream.Stream;

public class ChoiceCollectVisitor extends StreamVisitorTemplate<ChoiceBinding> {
    @Override
    public Stream<ChoiceBinding> visitChoiceBinding(ChoiceBinding choiceBinding) {
        return Stream.of(choiceBinding);
    }
}
