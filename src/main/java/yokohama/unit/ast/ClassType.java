package yokohama.unit.ast;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"})
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
