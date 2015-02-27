package yokohama.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.translator.DocyCompiler;

@AllArgsConstructor
public class CompileDocy implements Command {
    private final DocyCompiler compiler;
    FileInputStreamFactory fileInputStreamFactory;

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
                .withArgName("path")
                .withDescription("Specify where to find user class files and annotation processors")
                .create("classpath"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("path")
                .withDescription("Specify where to find user class files and annotation processors")
                .create("cp"));
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
                .withArgName("directory")
                .withDescription("Base directory for docy files")
                .create("basedir"));
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
            Arrays.asList("nowarn", "verbose", "classpath", "cp", "d", "target");

        try {
            CommandLine commandLine = new BasicParser().parse(options, args);
            if (commandLine.hasOption("help")) {
                PrintWriter pw = new PrintWriter(err);
                new HelpFormatter().printHelp(
                        pw,
                        80,
                        "docyc <options> <source files>",
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
            URI baseDir = Paths.get(commandLine.getOptionValue("basedir"), "").toUri();
            List<String> javacArgs =
                    extractOptions(
                            Arrays.asList(commandLine.getOptions()),
                            javacOptions);
            @SuppressWarnings("unchecked") List<String> files = commandLine.getArgList();
            for (String file : files) {
                String className = FilenameUtils.getBaseName(file);
                Path path = Paths.get(file).toAbsolutePath();
                URI uri = path.toUri();
                URI relativeUri = baseDir.relativize(uri).resolve(".");
                String packageName = StringUtils.removeEnd(relativeUri.toString(),"/").replace("/", ".");
                boolean success = compiler.compile(
                        path,
                        fileInputStreamFactory.create(path.toFile()),
                        className,
                        packageName,
                        javacArgs);
                if (!success) return Command.EXIT_FAILURE;
            }
            return Command.EXIT_SUCCESS;
        } catch (UnrecognizedOptionException e) {
            err.println("docyc: invalid flag: " + e.getOption());
            err.println("Usage: docyc <options> <source files>");
            err.println("use -help for a list of possible options");
            return Command.EXIT_FAILURE;
        } catch (ParseException|IOException e) {
            err.println("docyc: " + e.getMessage());
            err.println("Usage: docy <options> <source files>");
            err.println("use -help for a list of possible options");
            return Command.EXIT_FAILURE;
        }
    }

}
