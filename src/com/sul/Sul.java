package com.sul;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Sul {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();
    public static void main(String[] args) throws IOException {
        if(args.length > 1) {
            System.out.println("Usage: java Sul path");
            System.exit(1);
        } else if(args.length == 0) {
            runCommandLine();
        } else {
            runScript(args[0]);
        }
    }
    private static void runScript(String path) throws IOException {
        byte []bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes));
    }
    private static void runCommandLine() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(input);
        for(;;){
            String line = buffer.readLine();
            if(line == null) break;
            run(line);
            if(hadError) {
                hadError = false;
                System.exit(2);
            }
        }
    }
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens;
        tokens = scanner.getTokens();
//        for(Token token : tokens) {
//            System.out.println(token.lexeme + " " + token.value);
//        }
        Parser parser = new Parser(tokens);
        Expr parsedExpr = parser.parse();
        if(hadError) System.exit(2);
        ExprPrinter exprPrinter = new ExprPrinter();
        System.out.println(exprPrinter.print(parsedExpr));
        interpreter.interpret(parsedExpr);
        if(hadRuntimeError) System.exit(3);



    }
     static void error(int line, String message) {
        System.out.println("error in: " + line + ": " + message);
        hadError = true;
        //TODO
    }
    static void RuntimeError(RuntimeError error) {
        System.out.println("error in runtime: " + error.getMessage());
        System.out.println("In line: " + error.token.position);
        hadRuntimeError = true;
    }
}

