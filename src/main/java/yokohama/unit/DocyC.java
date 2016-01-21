package yokohama.unit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.springframework.beans.factory.BeanFactory;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import yokohama.unit.translator.CombinationStrategy;
import yokohama.unit.translator.DocyCompiler;

@AllArgsConstructor
public class DocyC implements Command {
    BeanFactory context;
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
                .withDescription("Generate code to check @Invariant annotation")
                .create("contract"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("base-packages")
                .withDescription("Base packages where converter classes are located")
                .create("converter"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("strategy")
                .withDescription("Choose combination test strategy")
                .create("combination"));
        options.addOption(OptionBuilder
                .withDescription("Emit Java code")
                .create("j"));
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

    static List<String> getClassPath(CommandLine commandLine) {
        String cp = commandLine.getOptionValue("cp");
        String classpath = commandLine.getOptionValue("classpath");
        if (cp != null) {
            return Arrays.asList(cp.split(File.pathSeparator));
        } else if (classpath != null) {
            return Arrays.asList(classpath.split(File.pathSeparator));
        } else {
            String env = System.getenv("DOCY_CLASSPATH");
            if (env != null) {
                return Arrays.asList(System.getenv("DOCY_CLASSPATH").split(File.pathSeparator));
            } else {
                return Arrays.asList();
            }

        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int run(InputStream in, PrintStream out, PrintStream err, String... args) {
        URI baseDir;
        Optional<Path> dest;
        boolean emitJava;
        boolean checkContract;
        CombinationStrategy combinationStrategy;
        List<String> converterBasePackages;
        List<String> classPath;
        List<String> javacArgs;
        List<String> files;

        try {
            Options options = constructOptions();
            List<String> javacOptions =
                    Arrays.asList("nowarn", "verbose", "target");

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
            baseDir = Paths.get(commandLine.getOptionValue("basedir"), "").toUri();
            String d = commandLine.getOptionValue("d");
            dest = d == null ? Optional.empty() : Optional.of(Paths.get(d));
            emitJava = commandLine.hasOption('j');
            checkContract = commandLine.hasOption("contract");
            combinationStrategy = context.getBean(
                    commandLine.getOptionValue("combination", "product"),
                    CombinationStrategy.class);
            String converter= commandLine.getOptionValue("converter");
            converterBasePackages = converter == null
                    ? Collections.emptyList()
                    : Arrays.asList(converter.split(","));
            classPath = getClassPath(commandLine);
            javacArgs = extractOptions(
                    Arrays.asList(commandLine.getOptions()),
                    javacOptions);
            files = commandLine.getArgList();
        } catch (UnrecognizedOptionException e) {
            err.println("docyc: invalid flag: " + e.getOption());
            err.println("Usage: docyc <options> <source files>");
            err.println("use -help for a list of possible options");
            return Command.EXIT_FAILURE;
        } catch (ParseException e) {
            err.println("docyc: " + e.getMessage());
            return Command.EXIT_FAILURE;
        }

        List<ErrorMessage> errors = files.stream().flatMap(file -> {
            String className = FilenameUtils.getBaseName(file);
            Path path = Paths.get(file).toAbsolutePath();
            URI uri = path.toUri();
            URI relativeUri = baseDir.relativize(uri).resolve(".");
            String packageName = StringUtils.removeEnd(relativeUri.toString(),"/").replace("/", ".");
            InputStream ins;
            try {
                ins = fileInputStreamFactory.create(path);
            } catch (IOException e) {
                Span span = Span.of(path);
                return Stream.of(new ErrorMessage(e.getMessage(), span));
            }
            return compiler.compile(
                        path,
                        ins,
                        className,
                        packageName,
                        classPath,
                        dest,
                        emitJava,
                        checkContract,
                        combinationStrategy,
                        converterBasePackages,
                        javacArgs)
                        .stream();
        }).collect(Collectors.toList());

        if (errors.isEmpty()) {
            return Command.EXIT_SUCCESS;
        } else {
            for (ErrorMessage errorMessage : errors) {
                err.println(errorMessage);
            }
            return Command.EXIT_FAILURE;
        }
    }

}
