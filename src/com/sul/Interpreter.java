package com.sul;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public void interpret(List<Stmt> stmtList) {
        try {
            for (Stmt stmt : stmtList) {
                executeStmt(stmt);
            }
        } catch (RuntimeError error) {
            Sul.RuntimeError(error);
        }
    }
    private void executeStmt(Stmt stmt) {
        stmt.accept(this);
    }
    private String makeValidString(Object value) {
        if(value == null) return "nihil";
        if(value instanceof Double) {
            String text = ((Double)value).toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }
    @Override
    public Object visitLiteral(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitBinary(Expr.Binary binary) {
        Object right = evaluate(binary.right);
        Object left = evaluate(binary.left);
        Token operator = binary.operator;
        switch (operator.type) {
            case PLUS:
                if(right instanceof String && left instanceof String) {
                    return (String)left + (String)right;
                }
                if(left instanceof Double && right instanceof Double) {
                    return (Double)left + (Double)right;
                }
                if(left instanceof Double && right instanceof String) {
                    return makeValidString(left) + right;
                }
                if(right instanceof Double && left instanceof String) {
                    return left + makeValidString(right);
                }
                throw new RuntimeError(operator, "cannot add two values");
            case MINUS:
                checkBinaryType(left, right, operator);
                return (Double)left - (Double)right;

            case STAR:
                checkBinaryType(left, right, operator);
                return (Double)left * (Double)right;

            case SLASH:
                checkBinaryType(left, right, operator);
                return (Double)left / (Double)right;
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case BANG_EQUAL:
                return !isEqual(left,right);
            case GREATER_EQUAL:
                checkBinaryType(left, right, operator);
                    return (Double)left >= (Double)right;
            case LESS_EQUAL:
                checkBinaryType(left, right, operator);
                return (Double)left <= (Double)right;
            case GREATER:
                checkBinaryType(left, right, operator);
                return (Double)left > (Double)right;
            case LESS:
                checkBinaryType(left, right, operator);
                return (Double)left < (Double)right;
        }
        return null;
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null) {
            return true;
        }
        if(left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }
    @Override
    public Object visitUnary(Expr.Unary unary) {
        Object expr = evaluate(unary.expression);
        Token operator = unary.operator;
        switch (operator.type) {
            case MINUS:
                checkUnaryType(expr, operator);
                return  -(double)expr;
            case BANG:
                checkUnaryType(expr, operator);
                return !isTruthy(expr);
        }
        return null;
    }
    private boolean isTruthy(Object expr) {
        if(expr instanceof Boolean) {
            return (Boolean)expr;
        }
        return expr != null;
    }

    @Override
    public Object visitGrouping(Expr.Grouping grouping) {
        Expr expr = grouping.expr;
        return expr.accept(this);
    }

    @Override
    public Object visitVariable(Expr.Variable variable) {
        return Sul.env.get(variable.name);
    }

    @Override
    public Object visitAssigment(Expr.Assigment assigment) {
        Token name = assigment.name;
        Object value = evaluate(assigment.value);
        Sul.env.assign(name, value);
        return null;
    }

    @Override
    public Void visitExpression(Stmt.Expression expressionStmt) {
        evaluate(expressionStmt.expr);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print printStmt) {
        Object value = evaluate(printStmt.expr);
        String valueString = makeValidString(value);
        System.out.println(valueString);
        return null;

    }

    @Override
    public Void visitDecl(Stmt.Decl decl) {
        if(decl.expr==null) Sul.env.put(decl.identifier.lexeme, null);
        else Sul.env.put(decl.identifier.lexeme, evaluate(decl.expr));
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    private void checkUnaryType(Object expr, Token operator) {
        if(expr instanceof Double) return;
        if(expr instanceof String) {
            throw new RuntimeError(operator, "operator must be a number");
        }
    }
    private void checkBinaryType(Object left,Object right, Token operator) {
        switch (operator.type) {
            case SLASH:
                if((Double)right == 0)
                    throw new RuntimeError(operator, "cannot divide by 0");
        }
        if(left instanceof Double && right instanceof Double) return;
        if(left instanceof String || right instanceof String)
            throw new RuntimeError(operator, "operator must be a number");
    }



}
