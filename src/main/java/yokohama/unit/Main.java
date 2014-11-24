package yokohama.unit;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

public class Main implements Command {

    static Options constructOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder
                .withDescription("Generate no warnings")
                .create("nowarn"));
        options.addOption(OptionBuilder
                .withDescription("Output messages about what the compiler is doing")
                .create("verbose"));
        options.addOption(OptionBuilder
                .withDescription("Print a synopsis of standard options")
                .create("help"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("encoding")
                .withDescription("Specify character encoding used by source files")
                .create("encoding"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("directory")
                .withDescription("Specify where to place generated class files")
                .create("d"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("release")
                .withDescription("Generate class files for specific VM version")
                .create("target"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("package")
                .withDescription("Package name for generated classes")
                .create("p"));
        return options;
    }

    static List<String> extractOptions(List<Option> options, List<String> extractedOptions) {
        return options.stream()
                .filter(option -> extractedOptions.contains(option.getOpt()) )
                .flatMap(option ->
                        option.hasArg() ? Stream.of("-" + option.getOpt(), option.getValue())
                                        : Stream.of("-" + option.getOpt()))
                .collect(Collectors.toList());
    }

    @Override
    public int run(InputStream in, PrintStream out, PrintStream err, String... args) {
        Options options = constructOptions();
        List<String> javacOptions =
            Arrays.asList("nowarn", "verbose", "d", "target");

        try {
            CommandLine commandLine = new BasicParser().parse(options, args);
            if (commandLine.hasOption("help")) {
                new HelpFormatter().printHelp(
                        new PrintWriter(err),
                        80,
                        "docy <options> <source files>",
                        "",
                        options,
                        0,
                        0,
                        ""
                );
                return Command.EXIT_SUCCESS;
            }
            List<String> javacArgs =
                    extractOptions(
                            Arrays.asList(commandLine.getOptions()),
                            javacOptions);
            List<String> files = commandLine.getArgList();
            throw new UnsupportedOperationException("unimplemented yet");
        } catch (UnrecognizedOptionException e) {
            err.println("docy: invalid flag: " + e.getOption());
            err.println("Usage: docy <options> <source files>");
            err.println("use -help for a list of possible options");
            return Command.EXIT_FAILURE;
        } catch (ParseException e) {
            err.println("docy: " + e.getMessage());
            err.println("Usage: docy <options> <source files>");
            err.println("use -help for a list of possible options");
            return Command.EXIT_FAILURE;
        }
    }

    public static void main(String args[]) {
        int status = new Main().run(System.in, System.out, System.err, args);
        System.exit(status);
    }
    
}
