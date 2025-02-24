package com.sul;

public class ExprPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }
    @Override
    public String  visitLiteral(Expr.Literal literal) {
        if(literal.value == null) return "null";
        return literal.value.toString();
    }

    @Override
    public String visitBinary(Expr.Binary binary) {
        return parenthesize(binary.operator.lexeme, binary.left, binary.right);
    }

    @Override
    public String visitUnary(Expr.Unary unary) {
        return parenthesize(unary.operator.lexeme, unary.expression);
    }

    @Override
    public String visitGrouping(Expr.Grouping grouping) {
        return parenthesize("grouping", grouping.expr);
    }

    @Override
    public String visitVariable(Expr.Variable variable) {
        return variable.name.lexeme;
    }

    @Override
    public String visitAssigment(Expr.Assigment assigment) {
        return "";
    }

    @Override
    public String visitOr(Expr.Or or) {
        return "";
    }

    @Override
    public String visitAnd(Expr.And and) {
        return "";
    }

    private String parenthesize(String operator, Expr ...exprs) {
        StringBuilder msg = new StringBuilder();
        msg.append("(");
        msg.append(operator);
        for (Expr expr : exprs) {
            msg.append(" ");
            msg.append(expr.accept(this));
        }
        msg.append(")");
        return msg.toString();
    }
}
