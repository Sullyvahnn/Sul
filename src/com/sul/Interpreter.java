package com.sul;

public class Interpreter implements Expr.Visitor<Object> {
    public void interpret(Expr expr) {
        try {
            Object result = expr.accept(this);
            String resultString = makeValidString(result);
            System.out.println(resultString);
        } catch (RuntimeError error) {
            Sul.RuntimeError(error);
        }
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
            case MINUS:
                if(right instanceof Double && left instanceof Double) {
                    return (Double)left - (Double)right;
                }
            case STAR:
                if(right instanceof Double && left instanceof Double) {
                    return (Double)left * (Double)right;
                }
            case SLASH:
                if(right instanceof Double && left instanceof Double) {
                    return (Double)left / (Double)right;
                }
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case BANG_EQUAL:
                return !isEqual(left,right);
            case GREATER_EQUAL:
                if(left instanceof Double && right instanceof Double) {
                    return (Double)left >= (Double)right;
                }
            case LESS_EQUAL:
                if(left instanceof Double && right instanceof Double) {
                    return (Double)left <= (Double)right;
                }
            case GREATER:
                if(left instanceof Double && right instanceof Double) {
                    return (Double)left > (Double)right;
                }
            case LESS:
                if(left instanceof Double && right instanceof Double) {
                    return (Double)left < (Double)right;
                }

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
        return switch (operator.type) {
            case MINUS -> -(double) expr;
            case BANG -> !isTruthy(expr);
            default -> null;
        };
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
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    private void checkUnaryType(Object expr, Token operator) {
        if(expr instanceof Double) {
            return;
        }
        if(expr instanceof String) {
            throw new RuntimeError(operator, "cannot deny String");
        }
    }
}
