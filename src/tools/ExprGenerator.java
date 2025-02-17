package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public abstract class ExprGenerator {
    static final List<String> types = Arrays.asList(
            "Literal: Object value",
            "Binary: Expr left, Token operator, Expr right",
            "Unary: Token operator, Expr expression",
            "Operator: Token operator"
    );
    static PrintWriter writer;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Unable to select path");
            System.exit(1);
        }

        final String dir = args[0];

        generateASTCode(dir, "Expr");
    }
    private static void generateASTCode(String dir, String name)
    throws IOException {
        String full_path = dir + "/" + name + ".java";
        writer = new PrintWriter(full_path);
        writer.println("package com.sul;");
        writer.println("public abstract class " + name + " {");
        writer.println("\tabstract void accept(Visitor v);");
        generateVisitorInt();
        fillExpressions(name);
        writer.println("}");
        writer.close();
    }
    private static void fillExpressions(String name) {
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String constructorExpr = type.split(":")[1].trim();
            String []fields = type.split(":")[1].trim().split(",");
            writer.println("\tstatic class " + className + " extends " + name + " {");
            for (String field : fields) {
                String fieldType = field.trim().split(" ")[0];
                String fieldName = field.trim().split(" ")[1];
                writer.println("\t\t" + fieldType + " " + fieldName + ";");
            }
            // constructor
            writer.println("\t\t" + className + "(" + constructorExpr + ") {");
            for(String field : fields) {
                String fieldName = field.trim().split(" ")[1];
                writer.println("            this." + fieldName + " = " + fieldName + ";");
            }
            writer.println("\t\t}");
            defineVisitor(className);
            writer.println("\t}");
        }
    }
    private static void defineVisitor(String className) {
        writer.println("\t\t@Override");
        writer.println("\t\tvoid accept(Visitor visitor) {");
        writer.println("\t\t\tvisitor.visit" + className + "(this);");
        writer.println("\t\t}");
    }
    private static void generateVisitorInt() {
        writer.println("\tinterface Visitor {");
        for(String type: types) {
            String classType = type.split(":")[0].trim();
            writer.println("\t\tvoid visit" + classType + " (" + classType + " " +
                    classType.toLowerCase() + ");");
        }

        writer.println("\t}");
    }
}
