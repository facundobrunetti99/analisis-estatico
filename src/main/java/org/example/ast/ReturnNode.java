package org.example.ast;
public class ReturnNode extends StatementNode {
    public final ExpressionNode expr;
    public ReturnNode(ExpressionNode expr) { this.expr = expr; }
    public String toString() { return "return " + expr; }
}
