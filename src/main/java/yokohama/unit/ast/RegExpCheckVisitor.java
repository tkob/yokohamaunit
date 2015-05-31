package yokohama.unit.ast;

import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.position.ErrorMessage;

public class RegExpCheckVisitor extends StreamVisitorTemplate<ErrorMessage> {
    public List<ErrorMessage> check(Group group) {
        return visitGroup(group).collect(Collectors.toList());
    }

    @Override
    public Stream<ErrorMessage> visitRegExpPattern(
            RegExpPattern regExpPattern) {
        try {
            java.util.regex.Pattern.compile(regExpPattern.getRegexp());
        } catch(PatternSyntaxException e) {
            return Stream.of(
                    new ErrorMessage(e.getMessage(), regExpPattern.getSpan()));
        }
        return Stream.empty();
    }
}
