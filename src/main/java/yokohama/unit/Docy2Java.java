package yokohama.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import yokohama.unit.translator.TranslatorUtils;

public class Docy2Java implements Command {
    static Options constructOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription("Package name of generated source code")
                .create("package"));
        options.addOption(OptionBuilder
                .withDescription("Print a synopsis of standard options")
                .create("help"));
        return options;
    }
    @Override
    public int run(InputStream in, PrintStream out, PrintStream err, String... args) {
        try {
            Options options = constructOptions();
            CommandLine commandLine = new BasicParser().parse(options, args);
            if (commandLine.hasOption("help")) {
                PrintWriter pw = new PrintWriter(err);
                new HelpFormatter().printHelp(
                        pw,
                        80,
                        "docy2java <options> <source file>",
                        "",
                        options,
                        1,
                        1,
                        "",
                        true
                );
                pw.flush();
                return Command.EXIT_SUCCESS;
            }
            @SuppressWarnings("unchecked") List<String> files = commandLine.getArgList();
            String file = files.get(0);
            String className = FilenameUtils.getBaseName(file);
            Path path = Paths.get(file).toAbsolutePath();
            URI uri = path.toUri();
            String packageName = commandLine.getOptionValue("package");
            String docy = IOUtils.toString(uri);
            String java = TranslatorUtils.docyToJava(
                    Optional.of(path),
                    docy,
                    className,
                    packageName);
            out.print(java);
            return Command.EXIT_SUCCESS;
        } catch (ParseException|IOException e) {
            err.println("docy2java: " + e.getMessage());
            err.println("Usage: docy2java <options> <source file>");
            err.println("use -help for a list of possible options");
            return Command.EXIT_FAILURE;
        }
    }
}
