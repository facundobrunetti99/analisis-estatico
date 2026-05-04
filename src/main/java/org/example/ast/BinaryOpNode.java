package org.example.ast;
public class BinaryOpNode extends ExpressionNode {
    public final ExpressionNode left, right;
    public final String op;
    public BinaryOpNode(ExpressionNode left, String op, ExpressionNode right) {
        this.left = left; this.op = op; this.right = right;
    }
    public String toString() { return left + " " + op + " " + right; }
}
