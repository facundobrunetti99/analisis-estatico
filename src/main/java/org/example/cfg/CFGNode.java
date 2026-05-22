package org.example.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *bloque basico en el CFG.
 */
public class CFGNode {
    private static int counter = 0;

    public final int id;
    public final String label;
    public final List<CFGNode> successors   = new ArrayList<>();
    public final List<CFGNode> predecessors = new ArrayList<>();
    private String definedVariable;
    private final Set<String> usedVariables = new LinkedHashSet<>();
    public boolean isEntry = false;
    public boolean isExit  = false;

    public CFGNode(String label) {
        this.id    = counter++;
        this.label = label;
    }

    public static void resetCounter() { counter = 0; }

    public void addSuccessor(CFGNode node) {
        if (!successors.contains(node)) {
            successors.add(node);
            node.predecessors.add(this);
        }
    }

    public void setDefinedVariable(String variable) {
        this.definedVariable = variable;
    }

    public String getDefinedVariable() {
        return definedVariable;
    }

    public void addUsedVariable(String variable) {
        usedVariables.add(variable);
    }

    public void addUsedVariables(Set<String> variables) {
        usedVariables.addAll(variables);
    }

    public Set<String> getUsedVariables() {
        return Collections.unmodifiableSet(usedVariables);
    }

    public String dotId()    { return "B" + id; }

    public String dotLabel() {
        if (isEntry) return "ENTRY";
        if (isExit)  return "EXIT";
        return label.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override public String toString() { return "B" + id + "[" + label + "]"; }
}
