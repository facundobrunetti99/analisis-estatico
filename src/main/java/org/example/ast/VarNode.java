package org.example.ast;
public class VarNode extends ExpressionNode {
    public final String name;
    public VarNode(String name) { this.name = name; }
    public String toString() { return name; }
}
