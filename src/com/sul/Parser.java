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
        while(!atTheEnd()) {
            //todo
        }
    }
    public boolean atTheEnd() {
        return current == tokens.size() - 1;
    }
    public Token next() {
        return tokens.get(current++);
    }
}
