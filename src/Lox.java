import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

// TODO: More verbose error messages

public class Lox {
    // Has an error occured? We don't want to run code with errors!!
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // Reads the passed file and executes it
    private static void runFile(String path) throws IOException {
        // Reads all bytes from file, file is closed when all bytes have been read
        // or and error is thrown.
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        //
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) {
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        // Used to bridge from byte streams to character streams
        InputStreamReader input = new InputStreamReader(System.in);
        // Reads text from the converted character stream
        BufferedReader reader = new BufferedReader(input);
        // Same as while(true)
        // REPL - Read, Eval, Print, Loop
        for (;;) {
            System.out.println("> ");
            // Read next line
            String line = reader.readLine();

            if (line == null) break;
            run(line);
            // Reset flag in order to not kill the entire session.
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = new scanner.scanTokens();

        // Print tokens
        for (Token token :tokens) {
            System.out.println(token);
        }
    }

    // Report error
    static void error(int line, String message) {
        report(line, "", message);
    }
    // Prints the line number and error message of said error
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

}
