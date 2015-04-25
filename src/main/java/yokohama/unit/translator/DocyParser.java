package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;
import yokohama.unit.position.ErrorMessage;

public interface DocyParser {
    GroupContext parse(
            Path docyPath,
            InputStream ins,
            List<? super ErrorMessage> errors) throws IOException;
}
