package com.sul;

import java.util.HashMap;

public class Env {
    private Env previous;
    private final HashMap<String,Object> env;
    public void put(Token name, Object value) {
        if(env.containsKey(name.lexeme))
            Sul.error(name.position,"variable: " + name.lexeme + " already exists");
        env.put(name.lexeme, value);
    }
    public Object get(Token name) {
        if(env.containsKey(name.lexeme)) {
            return env.get(name.lexeme);
        }
        if(previous!=null){
            return previous.get(name);

        }
        Sul.RuntimeError(new RuntimeError(name, "cannot use not declared variable"));
        System.exit(1);
        return null;
    }
    public void assign(Token name, Object value) {
        if(env.containsKey(name.lexeme)) {
            env.put(name.lexeme, value);
            return;
        }
        if(previous != null) {
            previous.assign(name, value);
            return;
        }
        Sul.error(name.position, "Failed to assign variable, it doesn't exist: " + name.lexeme);

    }
    Env() {
        env = new HashMap<>();
        previous = null;
    }
    Env(Env previous) {
        this.previous = previous;
        env = new HashMap<>();
    }
}
