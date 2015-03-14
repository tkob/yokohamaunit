package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.ast.Kind;

@Value
public class PrimitiveType implements NonArrayType {
    private Kind kind;

    @Override
    public String getText() {
        return kind.name().toLowerCase();
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
