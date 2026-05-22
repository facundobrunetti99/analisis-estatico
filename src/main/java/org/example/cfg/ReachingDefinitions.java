package org.example.cfg;

import java.util.*;

public class ReachingDefinitions {
    private final List<CFGNode> nodes;
    private final Map<CFGNode, Set<Definition>> in = new LinkedHashMap<>();
    private final Map<CFGNode, Set<Definition>> out = new LinkedHashMap<>();
    private final Map<CFGNode, Set<Definition>> gen = new LinkedHashMap<>();
    private final Map<CFGNode, Set<Definition>> kill = new LinkedHashMap<>();
    private final Set<Definition> allDefinitions = new LinkedHashSet<>();

    public ReachingDefinitions(List<CFGNode> nodes) {
        this.nodes = nodes;
    }

    public void compute() {
        collectDefinitions();
        initializeSets();

        boolean changed = true;
        while (changed) {
            changed = false;
            for (CFGNode n : nodes) {
                Set<Definition> newIn = new LinkedHashSet<>();
                for (CFGNode pred : n.predecessors) {
                    newIn.addAll(out.getOrDefault(pred, Collections.emptySet()));
                }

                Set<Definition> newOut = new LinkedHashSet<>(newIn);
                newOut.removeAll(kill.getOrDefault(n, Collections.emptySet()));
                newOut.addAll(gen.getOrDefault(n, Collections.emptySet()));

                if (!newIn.equals(in.get(n)) || !newOut.equals(out.get(n))) {
                    in.put(n, newIn);
                    out.put(n, newOut);
                    changed = true;
                }
            }
        }
    }

    private void collectDefinitions() {
        allDefinitions.clear();
        for (CFGNode n : nodes) {
            if (n.getDefinedVariable() != null) {
                allDefinitions.add(new Definition(n.getDefinedVariable(), n));
            }
        }
    }

    private void initializeSets() {
        for (CFGNode n : nodes) {
            Set<Definition> genSet = new LinkedHashSet<>();
            Set<Definition> killSet = new LinkedHashSet<>();

            String variable = n.getDefinedVariable();
            if (variable != null) {
                Definition ownDefinition = new Definition(variable, n);
                genSet.add(ownDefinition);
                for (Definition d : allDefinitions) {
                    if (d.variable.equals(variable) && d.node != n) {
                        killSet.add(d);
                    }
                }
            }

            gen.put(n, genSet);
            kill.put(n, killSet);
            in.put(n, new LinkedHashSet<>());
            out.put(n, new LinkedHashSet<>(genSet));
        }
    }

    public Set<Definition> getIn(CFGNode n) {
        return in.getOrDefault(n, Collections.emptySet());
    }

    public Set<Definition> getOut(CFGNode n) {
        return out.getOrDefault(n, Collections.emptySet());
    }

    public Map<CFGNode, Set<Definition>> getAllIn() {
        return in;
    }
}
