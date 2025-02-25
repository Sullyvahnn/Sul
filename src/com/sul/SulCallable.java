package com.sul;

import java.util.List;

interface SulCallable {
    Object call(Interpreter interpreter, List<Object> arguments);
    int arity();
}
