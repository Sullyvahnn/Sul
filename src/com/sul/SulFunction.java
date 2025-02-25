package com.sul;

import java.util.List;

public class SulFunction implements SulCallable{
    private final Env closure;
    private final Stmt.FunctionDecl declaration;
    SulFunction(Stmt.FunctionDecl declaration, Env closure) {
        this.declaration = declaration;
        this.closure = closure;
    }
    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        Env environment = new Env(closure);
        for (int i = 0; i < declaration.args.size(); i++) {
            environment.put(declaration.args.get(i),
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
    @Override
    public int arity() {
        return declaration.args.size();
    }
    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

}
