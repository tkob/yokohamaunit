package yokohama.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import yokohama.unit.translator.DocyCompilerImpl;

public class CommandFactory {
    private static final Map<String, Supplier<Command>> subCommands =
            new HashMap<String, Supplier<Command>>() {{
                put("docyc", () -> {
                    try (ConfigurableApplicationContext context =
                            new ClassPathXmlApplicationContext("applicationContext.xml")) {
                        return new DocyC(
                                context,
                                context.getBean(DocyCompilerImpl.class),
                                new FileInputStreamFactory());
                    }
                });
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
