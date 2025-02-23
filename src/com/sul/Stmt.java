package com.sul;
public abstract class Stmt {
	abstract <R> R accept(Visitor<R> v);
	interface Visitor<R> {
		R visitExpression (Expression expression);
		R visitPrint (Print print);
		R visitDecl(Decl decl);
	}
	static class Expression extends Stmt {
		Expr expr;
		Expression(Expr expr) {
            this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpression(this);
		}
	}
	static class Print extends Stmt {
		Expr expr;
		Print(Expr expr) {
            this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrint(this);
		}
	}
	static class Decl extends Stmt {
		Token identifier;
		Expr expr;
		Decl(Token identifier, Expr expr) {
			this.identifier = identifier;
			this.expr = expr;
		}
		<R> R accept(Visitor<R> visitor) {return visitor.visitDecl(this);}
	}
}
