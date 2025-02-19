package com.sul;

import java.util.List;

public class Parser {
    List<Token> tokens;
    int current;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }
    public void parse() {
        expression();
    }
    private boolean atTheEnd() {
        return current == tokens.size() - 1;
    }
    public Token next() {
        return tokens.get(current++);
    }
    private Token currentToken() {
        return tokens.get(current);
    }
    private Token previous() {
        return tokens.get(current-1);
    }
    private boolean match(TokenType ...types) {
        for (TokenType type : types) {
            if(currentToken().type == type) {
                next();
                return true;
            }
        }
        return false;
    }
    private Expr expression() {
        return equality();
    }
    private Expr equality() {
        Expr expr = comparision();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Expr right = comparision();
            expr = new Expr.Binary(expr, previous(), right);
        }
        return expr;
    }
    private Expr comparision() {
        Expr expr = term();

        while(match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            Expr right = term();
            expr = new Expr.Binary(expr, previous(), right);
        }
        return expr;
    }
    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.PLUS, TokenType.MINUS)) {
            Expr right = factor();
            expr = new Expr.Binary(expr, previous(), right);
        }
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.STAR, TokenType.SLASH)) {
            Expr right = unary();
            expr = new Expr.Binary(expr, previous(), right);
        }
        return expr;
    }
    private Expr unary() {
        if(match(TokenType.MINUS, TokenType.BANG)) {
            Expr expr = unary();
            expr = new Expr.Unary(previous(), expr);
        }
        return primary();
    }
    private Expr primary() {
        Expr expr;
        switch(currentToken().type) {
            case STRING, NUMBER -> expr = new Expr.Literal(previous().lexeme);
            case NULL -> expr = new Expr.Literal(null);
            case FALSE -> expr = new Expr.Literal(false);
            case TRUE -> expr = new Expr.Literal(true);
            case LEFT_PAREN -> expr = expression();
            default -> expr = primary();
        }
        return expr;
    }
}
