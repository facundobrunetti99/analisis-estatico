package org.example.ast;
public class NumNode extends ExpressionNode {
    public final int value;
    public NumNode(int value) { this.value = value; }
    public String toString() { return String.valueOf(value); }
}
