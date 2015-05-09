package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.Lists;

@Value
@EqualsAndHashCode(exclude={"span"})
public class CodeBlock implements Definition {
    Heading heading;
    String lang;
    List<String> lines;    
    Span span;

    public String getCode(
            String lineSeparator, boolean appendLineSeparatorAtTheEnd) {
        StringBuilder sb = new StringBuilder();
        Lists.mapInitAndLast(lines,
                line -> {
                    sb.append(line);
                    sb.append(lineSeparator);
                    return null;
                },
                line -> {
                    sb.append(line);
                    if (appendLineSeparatorAtTheEnd) {
                        sb.append(lineSeparator);
                    }
                    return null;
                });
        return sb.toString();
    }

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitCodeBlock(this);
    }
}
