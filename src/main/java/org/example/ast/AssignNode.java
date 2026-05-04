package org.example.ast;
public class AssignNode extends StatementNode {
    public final String var;
    public final ExpressionNode expr;
    public AssignNode(String var, ExpressionNode expr) {
        this.var = var; this.expr = expr;
    }
    public String toString() { return var + " = " + expr; }
}
