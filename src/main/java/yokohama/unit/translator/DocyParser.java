package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

public interface DocyParser {
    GroupContext parse(InputStream ins, List<? super ErrorMessage> errors) throws IOException;
}
