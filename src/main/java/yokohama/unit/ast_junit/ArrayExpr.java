package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import java.util.List;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.util.SBuilder;

@Value
public class ArrayExpr implements Expr {
    Type type;
    List<Sym> contents;

    void getExpr(SBuilder sb, Type type, String name) {
        NonArrayType componentType = type.getNonArrayType();
        int dims = type.getDims();
        int size = contents.size();
        sb.appendln(name, " = new ", componentType.getText(), "[", size, "]",
                StringUtils.repeat("[]", dims - 1), ";");
        for (int i = 0; i < size; i++) {
            sb.appendln(name, "[", i, "] = ", contents.get(i).getName(), ";");
        }
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitArrayExpr(this);
    }
}
