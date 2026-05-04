package org.example.ast;
import java.util.List;
public class IfNode extends StatementNode {
    public final ExpressionNode condition;
    public final List<StatementNode> thenBranch, elseBranch;
    public IfNode(ExpressionNode condition, List<StatementNode> thenBranch, List<StatementNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    public String toString() { return "if (" + condition + ")"; }
}
