package org.example.cfg;

import org.example.ast.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CFGBuilder {

    private CFGNode entry;
    private CFGNode exit;

    public CFGBuilder() {
        CFGNode.resetCounter();
        entry = new CFGNode("ENTRY");
        entry.isEntry = true;
       
    }

    public CFGNode getEntry() { return entry; }
    public CFGNode getExit()  { return exit;  }

    public void build(ProgramNode program) {
        
        exit = new CFGNode("EXIT");
        exit.isExit = true;

        CFGNode first = buildStatements(program.statements, exit);
        entry.addSuccessor(first);
    }

    private CFGNode buildStatements(List<StatementNode> stmts, CFGNode next) {
        CFGNode current = next;
        for (int i = stmts.size() - 1; i >= 0; i--) {
            current = buildStatement(stmts.get(i), current);
        }
        return current;
    }

    private CFGNode buildStatement(StatementNode stmt, CFGNode next) {
        if (stmt instanceof AssignNode)  return buildAssign((AssignNode) stmt, next);
        if (stmt instanceof ReturnNode)  return buildReturn((ReturnNode) stmt);
        if (stmt instanceof IfNode)      return buildIf((IfNode) stmt, next);
        if (stmt instanceof WhileNode)   return buildWhile((WhileNode) stmt, next);
        throw new RuntimeException("Sentencia desconocida: " + stmt.getClass());
    }

    private CFGNode buildAssign(AssignNode a, CFGNode next) {
        CFGNode block = new CFGNode(a.var + " = " + a.expr);
        block.setDefinedVariable(a.var);
        block.addUsedVariables(collectUses(a.expr));
        block.addSuccessor(next);
        return block;
    }

    private CFGNode buildReturn(ReturnNode r) {
        CFGNode block = new CFGNode("return " + r.expr);
        block.addUsedVariables(collectUses(r.expr));
        block.addSuccessor(exit);
        return block;
    }

    private CFGNode buildIf(IfNode ifNode, CFGNode next) {
        CFGNode thenFirst = buildStatements(ifNode.thenBranch, next);
        CFGNode elseFirst = buildStatements(ifNode.elseBranch, next);
        CFGNode condBlock = new CFGNode("if (" + ifNode.condition + ")");
        condBlock.addUsedVariables(collectUses(ifNode.condition));
        condBlock.addSuccessor(thenFirst);
        condBlock.addSuccessor(elseFirst);
        return condBlock;
    }

    private CFGNode buildWhile(WhileNode whileNode, CFGNode next) {
        CFGNode condBlock = new CFGNode("while (" + whileNode.condition + ")");
        condBlock.addUsedVariables(collectUses(whileNode.condition));
        CFGNode bodyFirst = buildStatements(whileNode.body, condBlock);
        condBlock.addSuccessor(bodyFirst);
        condBlock.addSuccessor(next);
        return condBlock;
    }

    private Set<String> collectUses(ExpressionNode expr) {
        Set<String> uses = new LinkedHashSet<>();
        collectUses(expr, uses);
        return uses;
    }

    private void collectUses(ExpressionNode expr, Set<String> uses) {
        if (expr instanceof VarNode) {
            uses.add(((VarNode) expr).name);
        } else if (expr instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) expr;
            collectUses(bin.left, uses);
            collectUses(bin.right, uses);
        }
    }
}
