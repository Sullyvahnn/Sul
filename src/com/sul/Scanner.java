package com.sul;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    final String source;
    private int start = 0;
    private int current;
    private int line = 1;
    boolean finished;
    private List<Token> tokens = new ArrayList<>();

    Scanner(String source) {
        this.source = source;
        this.current = 0;
        this.finished = false;
        this.tokens = scan();
    }

    private List<Token> scan() {
        while (!finished) {
            start = current;
            scanToken();

            if (current >= source.length()) finished = true;
        }
        return tokens;
    }

    private void scanToken() {
        char current_char = source.charAt(current);
        char next = getNext();
        if (isLetter(current_char, false)) {
            generateEOFToken(next);
            return;
        } else if(isNumber(current_char)) {
            generateNumberToken(next);
            return;
        }
        switch (next) {
            case '\n':
                line++;
                break;
            case '\r', ' ':
                break;
            case '=':
                addToken(checkNextValue('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL, null);
                break;
            case '!':
                addToken(checkNextValue('=') ? TokenType.BANG_EQUAL : TokenType.BANG, null);
                break;
            case '<':
                addToken(checkNextValue('=') ? TokenType.LESS_EQUAL : TokenType.LESS, null);
                break;
            case '>':
                addToken(checkNextValue('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER, null);
                break;
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            default:
                Sul.error(line, "unrecognized token: " + next);

        }
    }

    private char getNext() {
        if (current >= source.length()) return '\0';
        return source.charAt(current++);
    }

    private void addToken(TokenType type, Object value) {
        String lexeme = source.substring(start, current);
        if (type == TokenType.EOF) type = checkForKeyword(lexeme);
        if (lexeme.isBlank()) return;
        Token token = new Token(type, line, lexeme, value);
        tokens.add(token);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private boolean checkNextValue(char next) {
        if (current == source.length()) return false;
        boolean val = source.charAt(current) == next;
        if (val) {
            current++;
        }
        return val;
    }

    private TokenType checkForKeyword(String candidate) {
        return switch (candidate) {
            case "if" -> TokenType.IF;
            case "else" -> TokenType.ELSE;
            case "and" -> TokenType.AND;
            case "or" -> TokenType.OR;
            case "false" -> TokenType.FALSE;
            case "true" -> TokenType.TRUE;
            case "null" -> TokenType.NULL;
            case "print" -> TokenType.PRINT;
            case "return" -> TokenType.RETURN;
            case "super" -> TokenType.SUPER;
            case "this" -> TokenType.THIS;
            case "while" -> TokenType.WHILE;
            case "var" -> TokenType.VAR;
            case "class" -> TokenType.CLASS;
            case "fun" -> TokenType.FUN;
            default -> TokenType.EOF;
        };
    }

    private void generateEOFToken(char next) {
        while (isLetter(next, true)) {
            next = getNext();
        }
        current--;
        String value = source.substring(start, current);
        addToken(TokenType.EOF, value);
    }
    private void generateNumberToken(char next) {
        while(isNumber(next)) {
            next = getNext();
            if(!isNumber(next) && next!='.' && current<source.length()) {
                current--;
            }
        }
        if(next=='.') {
            next = getNext();
            if(!isNumber(next)) {
                Sul.error(line, "unrecognized symbol: " + next);
                System.exit(1);
            } else {
                while(isNumber(next)) {
                    next = getNext();
                    if(!isNumber(next) && next!='.') {
                        current--;
                    }
                }
            }
        }
        else if(isLetter(next, false)) {
            String value = source.substring(start, current);
            Sul.error(line, "unrecognized number: " + value);
            System.exit(1);
        }
        String value = source.substring(start, current);
        if(value.endsWith(".")) Sul.error(line, "unrecognized number: " + value);
        addToken(TokenType.EOF, value);

    }
    private boolean isLetter(char c, boolean accept_number) {
        if(accept_number) {
            return (('A' <= c && c <='Z') || (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9'));
        }
        return (('A' <= c && c <='Z') || (c >= 'a' && c <= 'z'));
    }
    private boolean isNumber(char c) {
        return  (c >= '0' && c <= '9');
    }
}
