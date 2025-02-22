package com.sul;

enum TokenType {
    // single character
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COLON, COMMA, SEMICOLON, DOT, MINUS, PLUS, STAR, SLASH,
    // single-double charakters
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    // literals
    IDENTIFIER, STRING, NUMBER,
    // keywords
    AND, OR, ELSE, FALSE, TRUE, IF, NULL, PRINT,
    RETURN, SUPER, THIS, WHILE, CLASS, VAR, FUN, FOR,

    EOF
}
