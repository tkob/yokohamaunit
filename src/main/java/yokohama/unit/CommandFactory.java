package yokohama.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.StrBuilder;
import yokohama.unit.translator.DocyCompilerImpl;

public class CommandFactory {
    private static final Map<String, Supplier<Command>> subCommands =
            new HashMap<String, Supplier<Command>>() {{
                put("docyc", () -> new CompileDocy(new DocyCompilerImpl()));
            }};

    public Command create(String subCommand) {
        if (subCommands.containsKey(subCommand)) {
            return subCommands.get(subCommand).get();
        } else {
            throw new IllegalArgumentException(new StrBuilder()
                    .appendln("Unknown subcommand " + subCommand)
                    .appendln("Available subcomands are:")
                    .appendln(subCommands.keySet().stream().sorted().collect(Collectors.joining(", ")))
                    .toString()
            );
        }
    }
}
