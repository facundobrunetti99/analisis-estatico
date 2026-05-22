package org.example.cfg;

import java.util.*;

public class DDGBuilder {
    private final List<CFGNode> nodes;
    private final ReachingDefinitions reachingDefinitions;
    private final Map<CFGNode, Set<CFGNode>> ddg = new LinkedHashMap<>();
    private final Map<CFGNode, Map<CFGNode, Set<String>>> variablesByEdge = new LinkedHashMap<>();

    public DDGBuilder(List<CFGNode> nodes, ReachingDefinitions reachingDefinitions) {
        this.nodes = nodes;
        this.reachingDefinitions = reachingDefinitions;
        for (CFGNode n : nodes) {
            ddg.put(n, new LinkedHashSet<>());
            variablesByEdge.put(n, new LinkedHashMap<>());
        }
    }

    public void compute() {
        for (CFGNode useNode : nodes) {
            for (String usedVariable : useNode.getUsedVariables()) {
                for (Definition definition : reachingDefinitions.getIn(useNode)) {
                    if (definition.variable.equals(usedVariable)) {
                        addDependency(useNode, definition.node, usedVariable);
                    }
                }
            }
        }
    }

    private void addDependency(CFGNode useNode, CFGNode defNode, String variable) {
        ddg.get(useNode).add(defNode);
        variablesByEdge
            .computeIfAbsent(useNode, k -> new LinkedHashMap<>())
            .computeIfAbsent(defNode, k -> new LinkedHashSet<>())
            .add(variable);
    }

    /**retorna las definiciones de las que n depende por datos */
    public Set<CFGNode> getDependenciesOf(CFGNode n) {
        return ddg.getOrDefault(n, Collections.emptySet());
    }

    public Map<CFGNode, Set<CFGNode>> getAllDDG() {
        return ddg;
    }

    public Set<String> getVariables(CFGNode useNode, CFGNode defNode) {
        return variablesByEdge
            .getOrDefault(useNode, Collections.emptyMap())
            .getOrDefault(defNode, Collections.emptySet());
    }
}
