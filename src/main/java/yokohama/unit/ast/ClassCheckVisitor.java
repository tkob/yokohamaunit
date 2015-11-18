package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.util.ClassResolver;

@AllArgsConstructor
public class ClassCheckVisitor extends StreamVisitorTemplate<ErrorMessage> {
    private final ClassResolver classResolver;

    public List<ErrorMessage> check(Group group) {
        return visitGroup(group).collect(Collectors.toList());
    }

    @Override
    public Stream<ErrorMessage> visitClassType(ClassType classType) {
        String name = classType.getName();
        try {
            classResolver.lookup(name);
        } catch (ClassNotFoundException e) {
            return Stream.of(new ErrorMessage(
                    "cannot resolve class: " + name,
                    classType.getSpan()));
        }
        return Stream.empty();
    }
}
