package com.sul;

import java.util.List;

public abstract class Stmt {
	abstract <R> R accept(Visitor<R> v);
	interface Visitor<R> {
		R visitExpression (Expression expression);
		R visitPrint (Print print);
		R visitDecl(Decl decl);
		R visitBlock(Block block);
		R visitIfStmt(IfStmt ifStmt);
		R visitWhileStmt(WhileStmt whileStmt);
		R visitLoopControlStmt(LoopControlStmt loopControlStmt);
		R visitFunctionDecl(FunctionDecl functionDecl);
		R visitReturnStmt(ReturnStmt returnStmt);
	}
	public static class Expression extends Stmt {
		Expr expr;
		Expression(Expr expr) {
            this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpression(this);
		}
	}
	public static class Print extends Stmt {
		Expr expr;
		Print(Expr expr) {
            this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrint(this);
		}
	}
	public static class Decl extends Stmt {
		Token identifier;
		Expr expr;
		Decl(Token identifier, Expr expr) {
			this.identifier = identifier;
			this.expr = expr;
		}
		<R> R accept(Visitor<R> visitor) {return visitor.visitDecl(this);}
	}
	public static class Block extends Stmt {
		List<Stmt> stmts;
		Block(List<Stmt> stmts) {
			this.stmts = stmts;
		}
		<R> R accept(Visitor<R> visitor) {return visitor.visitBlock(this);}
	}
	public static class IfStmt extends Stmt {
		Expr condition;
		Stmt thenStmt;
		Stmt elseStmt;
		IfStmt(Expr condition, Stmt thenStmt, Stmt elseStmt) {
			this.condition = condition;
			this.thenStmt = thenStmt;
			this.elseStmt = elseStmt;
		}
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}
	}
	public static class WhileStmt extends Stmt {
		Expr condition;
		Stmt body;
		WhileStmt(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
	}
	public static class LoopControlStmt extends Stmt {
		Token keyWord;
		LoopControlStmt(Token keyWord) {
			this.keyWord = keyWord;
		}
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLoopControlStmt(this);
		}
	}
	public static class FunctionDecl extends Stmt {
		Token name;
		List<Token> args;
		List<Stmt> body;
		FunctionDecl(Token name, List<Token> args, List<Stmt> body) {
			this.name = name;
			this.args = args;
			this.body = body;
		}
		<R> R accept(Visitor<R> visitor) { return visitor.visitFunctionDecl(this);}
	}
	public static class ReturnStmt extends Stmt {
		Token keyWord;
		Expr value;
		ReturnStmt(Token keyWord, Expr value) {
			this.keyWord = keyWord;
			this.value = value;
		}
		<R> R accept(Visitor<R> visitor) {return visitor.visitReturnStmt(this);}
	}


}
