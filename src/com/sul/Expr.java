package com.sul;
public abstract class Expr {
	abstract <R> R accept(Visitor<R> v);
	interface Visitor<R> {
		R visitLiteral (Literal literal);
		R visitBinary (Binary binary);
		R visitUnary (Unary unary);
		R visitGrouping (Grouping grouping);
	}
	static class Literal extends Expr {
		Object value;
		Literal(Object value) {
            this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteral(this);
		}
	}
	static class Binary extends Expr {
		Expr left;
		Token operator;
		Expr right;
		Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinary(this);
		}
	}
	static class Unary extends Expr {
		Token operator;
		Expr expression;
		Unary(Token operator, Expr expression) {
            this.operator = operator;
            this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnary(this);
		}
	}
	static class Grouping extends Expr {
		Expr expr;
		Grouping(Expr expr) {
            this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGrouping(this);
		}
	}
}
