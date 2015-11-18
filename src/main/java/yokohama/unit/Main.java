package yokohama.unit;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Main implements Command {
    CommandFactory commandFactory;

    @Override
    public int run(InputStream in, PrintStream out, PrintStream err, String... args) {
        if (args.length < 1) {
            err.println("wrong # args: should be \"yokohamaunit <command> [<args>]\"");
            return Command.EXIT_FAILURE;
        }

        String subCommand = args[0];
        String[] realArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            Command command = commandFactory.create(subCommand);
            return command.run(System.in, System.out, System.err, realArgs);
        } catch (IllegalArgumentException e) {
            err.println("yokohamaunit: " + e.getMessage());
            return Command.EXIT_FAILURE;
        }
    }
    
    public static void main(String args[]) {
        Command main = new Main(new CommandFactory());
        int status = main.run(System.in, System.out, System.err, args);
        System.exit(status);
    }

}
