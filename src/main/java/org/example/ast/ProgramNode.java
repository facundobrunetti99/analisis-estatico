package org.example.ast;
import java.util.List;
public class ProgramNode extends ASTNode {
    public final String functionName;
    public final List<StatementNode> statements;
    public ProgramNode(String functionName, List<StatementNode> statements) {
        this.functionName = functionName;
        this.statements = statements;
    }
}
