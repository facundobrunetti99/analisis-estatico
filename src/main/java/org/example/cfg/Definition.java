package org.example.cfg;

import java.util.Objects;

public class Definition {
    public final String variable;
    public final CFGNode node;

    public Definition(String variable, CFGNode node) {
        this.variable = variable;
        this.node = node;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Definition)) return false;
        Definition other = (Definition) obj;
        return Objects.equals(variable, other.variable) && node == other.node;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, node);
    }

    @Override
    public String toString() {
        return variable + "@" + node.dotId();
    }
}
