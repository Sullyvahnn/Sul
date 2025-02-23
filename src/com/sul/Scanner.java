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
        this.source = source.trim();
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
        char next  = getNext();
        if (isLetter(next, true)) {
            generateEOFToken();
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
            case '/':
                if(checkNextValue('/'))
                    addToken(TokenType.COMMENT);
                else addToken(TokenType.SLASH);
                break;
            case '"':
                generateSTRINGToken();
                break;
            default:
                Sul.error(line, "unrecognized token: " + next);

        }
    }

    private char getNext() {
        if (current >= source.length()+1) return '\0';
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
    private void generateSTRINGToken(){
        while(!checkNextValue('"')) {
            getNext();
            if(current >= source.length()) {
                Sul.error(line, "expected: "+'"');
                System.exit(1);
            }
        }
        Object value = source.substring(start+1, current-1);
        addToken(TokenType.STRING, value);
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
            case "nihil" -> TokenType.NULL;
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

    private char peek() {
        if (current >= source.length()) return '\0';
        return source.charAt(current);
    }
    private void generateEOFToken() {
        char next=source.charAt(current-1);
        boolean isNumber = !(isLetter(next, false));
        while ((isLetter(peek(),true) || (peek()=='.') && isNumber)) {
            if(isLetter(next,false)) isNumber = false;
            next = getNext();
        }
        if (isLetter(next,false)) isNumber = false;
        String value = source.substring(start, current);
        Object valueInt = value;
        if(!isNumber) {
            char firstChar = value.charAt(0);
            if(!isLetter(firstChar, false)) {
                Sul.error(line, "unrecognized string: " + value);
                System.exit(1);
            }
        } else {
            int dotCount = value.length() - value.replace(".", "").length();
            if(value.endsWith(".") || dotCount>1) {
                Sul.error(line, "unrecognized number: " + value);
                System.exit(1);
            }
            valueInt = getValue(value);
        }
        if(valueInt instanceof Double)
            addToken(TokenType.NUMBER, valueInt);
        else addToken(TokenType.EOF, valueInt);

    }
    private boolean isLetter(char c, boolean accept_number) {
        if (accept_number) {
            return (('A' <= c && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9'));
        }
        return (('A' <= c && c <= 'Z') || (c >= 'a' && c <= 'z'));
    }

    private Object getValue(String s) {
        return Double.parseDouble(s);
    }
}
