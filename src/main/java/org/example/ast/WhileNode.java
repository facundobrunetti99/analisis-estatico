package org.example.ast;
import java.util.List;
public class WhileNode extends StatementNode {
    public final ExpressionNode condition;
    public final List<StatementNode> body;
    public WhileNode(ExpressionNode condition, List<StatementNode> body) {
        this.condition = condition;
        this.body = body;
    }
    public String toString() { return "while (" + condition + ")"; }
}
