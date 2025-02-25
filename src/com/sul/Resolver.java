package com.sul;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
    @Override
    public Void visitBlock(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.stmts);
        endScope();
        return null;
    }
    @Override
    public Void visitDecl(Stmt.Decl stmt) {
        declare(stmt.identifier);
        if (stmt.expr != null) {
            resolve(stmt.expr);
        }
        define(stmt.identifier);
        return null;
    }
    @Override
    public Void visitVariable(Expr.Variable expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Sul.error(expr.name.position,
                    "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
    @Override
    public Void visitAssigment(Expr.Assigment expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }
    @Override
    public Void visitFunctionDecl(Stmt.FunctionDecl stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }
    private void resolveFunction(
            Stmt.FunctionDecl function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.args) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }
    @Override
    public Void visitExpression(Stmt.Expression stmt) {
        resolve(stmt.expr);
        return null;
    }
    @Override
    public Void visitIfStmt(Stmt.IfStmt stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenStmt);
        if (stmt.elseStmt != null) resolve(stmt.elseStmt);
        return null;
    }
    @Override
    public Void visitPrint(Stmt.Print stmt) {
        resolve(stmt.expr);
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.ReturnStmt stmt) {
        if (currentFunction == FunctionType.NONE) {
            Sul.error(stmt.keyWord.position, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }
    @Override
    public Void visitWhileStmt(Stmt.WhileStmt stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitLoopControlStmt(Stmt.LoopControlStmt loopControlStmt) {
        return null;
    }

    @Override
    public Void visitBinary(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitCallExpr(Expr.CallExpr expr) {
        resolve(expr.name);

        for (Expr argument : expr.args) {
            resolve(argument);
        }

        return null;
    }
    @Override
    public Void visitGrouping(Expr.Grouping expr) {
        resolve(expr.expr);
        return null;
    }
    @Override
    public Void visitLiteral(Expr.Literal expr) {
        return null;
    }
    @Override
    public Void visitOr(Expr.Or expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitAnd(Expr.And expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitUnary(Expr.Unary expr) {
        resolve(expr.expression);
        return null;
    }
    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Sul.error(name.position,
                    "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }
    private void endScope() {
        scopes.pop();
    }
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }
    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }
    private void resolve(Expr expr) {
        expr.accept(this);
    }

}
enum FunctionType {
    NONE,
    FUNCTION
}