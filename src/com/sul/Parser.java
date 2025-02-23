package com.sul;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {

    }
    List<Token> tokens;
    int current;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }
    public List<Stmt> parse() {
        try{
            List<Stmt> statements = new ArrayList<>();
            while(!atTheEnd()) statements.add(statement());
            return statements;
        } catch (ParseError e) {
            Sul.hadError = true;
            return null;
        }
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
    private Stmt statement() {
        if(match(TokenType.PRINT)) {
            return printExpression();
        }
        return statementExpression();

    }
    private void stmtEndingCheck() {
        if(currentToken().type == TokenType.RIGHT_PAREN) {
            Sul.error(currentToken().position, "unexpected right parenthesis");
            System.exit(1);
        }
        if(currentToken().type == TokenType.RIGHT_BRACE) {
            Sul.error(currentToken().position, "unexpected right bracket");
            System.exit(1);
        }
    }
    private Stmt statementExpression() {
        Expr expr = expression();
        stmtEndingCheck();
        consume(TokenType.SEMICOLON, "Expected semicolon after expression");
        return new Stmt.Expression(expr);
    }
    private Stmt printExpression() {
        Expr expr = expression();
        stmtEndingCheck();
        consume(TokenType.SEMICOLON, "Expected semicolon after expression");
        return new Stmt.Print(expr);
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
        Expr expr;
        int finished = current;
        if(match(TokenType.NULL)) return new Expr.Literal(null);
        if(match(TokenType.STRING, TokenType.NUMBER)) {
            if(finished==current) expr = new Expr.Literal(currentToken().value);
            else expr = new Expr.Literal(previous().value);
            return expr;
        }
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.LEFT_PAREN)) {
            expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);

        }
        throw error(currentToken(), "Expect expression.");
    }
    private Token consume(TokenType tokenToConsume, String message) {
        if(currentToken().type == tokenToConsume) return next();
        throw error(currentToken(), message);
    }
    private ParseError error(Token token, String message) {
        Sul.error(currentToken().position, message);
        return new ParseError();
    }
    private void synchronize() {
        next();

        while (!atTheEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (currentToken().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            next();
        }
    }
}
