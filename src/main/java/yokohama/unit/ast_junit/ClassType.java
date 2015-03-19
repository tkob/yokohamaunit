package yokohama.unit.ast_junit;

import java.util.Optional;
import lombok.Value;

@Value
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    @Override
    public String getText() {
        return name;
    }

    public static ClassType of(yokohama.unit.ast.ClassType classType) {
        return new ClassType(
                classType.getName(),
                new Span(
                        Optional.empty(),
                        classType.getSpan().getStart(),
                        classType.getSpan().getEnd()));

    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
