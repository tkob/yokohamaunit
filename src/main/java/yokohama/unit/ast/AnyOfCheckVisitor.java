package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.position.ErrorMessage;

public class AnyOfCheckVisitor extends StreamVisitorTemplate<ErrorMessage> {
    public List<ErrorMessage> check(Group group) {
        return visitGroup(group).collect(Collectors.toList());
    }

    // 'any of' is allowed in a functional test
    @Override
    public Stream<ErrorMessage> visitTest(Test test) {
        return Stream.empty();
    }

    // 'any of' is allowed in (setup) phase of a four phase test
    @Override
    public Stream<ErrorMessage> visitPhase(Phase phase) {
        return Stream.empty();
    }

    // otherwise (i.e. in verify phase of four phase test), it is not allowed
    @Override
    public Stream<ErrorMessage> visitChoiceBinding(ChoiceBinding choiceBinding) {
        return Stream.of(
                new ErrorMessage("'any of' is not allowed here", choiceBinding.getSpan()));
    }
}
