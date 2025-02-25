package com.sul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private boolean breakState=false;
    final Env globals = new Env();
    private Env env = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();
    Interpreter() {
        Token clock = new Token(TokenType.EOF, 0,"clock", "clock");
        globals.put(clock, new SulCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }
    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }
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
        return lookUpVariable(variable.name, variable);
    }
    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return env.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitAssigment(Expr.Assigment assigment) {
        Token name = assigment.name;
        Object value = evaluate(assigment.value);
        Integer distance = locals.get(assigment);
        if (distance != null) {
            env.assignAt(distance, assigment.name, value);
        } else {
            globals.assign(assigment.name, value);
        }
        env.assign(name, value);
        return null;
    }

    @Override
    public Object visitOr(Expr.Or or) {
        Object left = evaluate(or.left);
        if(isTruthy(left)) {
            return true;
        }
        Object right = evaluate(or.right);
        return isTruthy(right) || isTruthy(left);
    }

    @Override
    public Object visitAnd(Expr.And and) {
        Object left = evaluate(and.left);
        if(!isTruthy(left)) {
            return false;
        }
        Object right = evaluate(and.right);
        return isTruthy(right) && isTruthy(left);
    }

    @Override
    public Object visitCallExpr(Expr.CallExpr callExpr) {
        Object name = evaluate(callExpr.name);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : callExpr.args) {
            arguments.add(evaluate(argument));
        }
        if (!(name instanceof SulCallable)) {
            throw new RuntimeError(callExpr.closureParent,
                    "Can only call functions and classes.");
        }
        SulCallable function = (SulCallable)name;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(callExpr.closureParent, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }
        return function.call(this, arguments);
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
        if(decl.expr==null) env.put(decl.identifier, null);
        else env.put(decl.identifier, evaluate(decl.expr));
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block block) {
        executeBlock(block.stmts, new Env(env));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.IfStmt ifStmt) {
       Object condition = evaluate(ifStmt.condition);
       if(isTruthy(condition)) {
           executeStmt(ifStmt.thenStmt);
       } else if(ifStmt.elseStmt != null) executeStmt(ifStmt.elseStmt);
       return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.WhileStmt whileStmt) {
        while(isTruthy(evaluate((whileStmt.condition))) && !breakState) {
            executeStmt(whileStmt.body);
        }
        breakState = false;
        return null;
    }

    @Override
    public Void visitLoopControlStmt(Stmt.LoopControlStmt loopControlStmt) {
        Token token = loopControlStmt.keyWord;
        if(token.type == TokenType.BREAK) breakState=true;
        return null;
    }

    @Override
    public Void visitFunctionDecl(Stmt.FunctionDecl functionDecl) {
        SulFunction function = new SulFunction(functionDecl, env);
        env.put(functionDecl.name, function);
        return null;
    }

    public void executeBlock(List<Stmt> block, Env env_) {
        Env previous = env;
        try {
            env = env_;
            for(Stmt stmt : block) {
                executeStmt(stmt);
            }
        } finally {
            env = previous;
        }

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
    @Override
    public Void visitReturnStmt(Stmt.ReturnStmt stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }



}
