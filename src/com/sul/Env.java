package com.sul;

import java.util.HashMap;

public class Env {
    private final HashMap<String,Object> env;
    public void put(String name, Object value) {
        env.put(name, value);
    }
    public Object get(Token name) {
        if(env.containsKey(name.lexeme)) {
            return env.get(name.lexeme);
        } else {
            Sul.error(name.position, "cannot access variable: " + name.lexeme);
        }
        return null;
    }
    public void assign(Token name, Object value) {
        if(env.containsKey(name.lexeme)) {
            env.put(name.lexeme, value);
            return;
        }
        Sul.error(name.position, "Failed to assign variable, it doesn't exist: " + name.lexeme);

    }
    Env() {
        env = new HashMap<>();
    }
}
