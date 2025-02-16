package com.sul;

public class Token {
    final TokenType type;
    final int position;
    final String lexeme;
    final Object value;
    public Token(TokenType type, int position, String lexeme, Object value) {
        this.type = type;
        this.position = position;
        this.lexeme = lexeme;
        this.value = value;
    }

}
