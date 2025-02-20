package com.sul;

import java.util.List;

public class Parser {
    List<Token> tokens;
    int current;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }
    public Expr parse() {
        return expression();
    }
    private boolean atTheEnd() {
        return current == tokens.size()-1;
    }
    public Token next() {
        if(!atTheEnd()) return tokens.get(current++);
        return previous();
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
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr comparison() {
        Expr expr = term();

        while(match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr unary() {
        if(match(TokenType.MINUS, TokenType.BANG)) {
            Token operator = previous();
            return new Expr.Unary(operator, unary());
        }
        return primary();
    }
    private Expr primary() {
        Expr expr = null;
        if(match(TokenType.NULL)) expr = new Expr.Literal(null);
        else if(match(TokenType.EOF)) {
            if(atTheEnd()) expr = new Expr.Literal(currentToken().value);
            else expr = new Expr.Literal(previous().value);
        }
        else if(match(TokenType.TRUE)) expr = new Expr.Literal(true);
        else if(match(TokenType.FALSE)) expr = new Expr.Literal(false);
        else if(match(TokenType.LEFT_PAREN)) {
            expr = expression();
            consume(")");
            expr = new Expr.Grouping(expr);
        }
//        else if(match(TokenType.RIGHT_PAREN)) {
//            Sul.error(currentToken().position, "invalid parenthesis");
//            System.exit(1);
//        }
        return expr;
    }
    private void consume(String tokenToConsume) {
        if(currentToken().lexeme.equals(tokenToConsume)) next();
        else Sul.error(currentToken().position, "expected " + tokenToConsume);
    }
}
