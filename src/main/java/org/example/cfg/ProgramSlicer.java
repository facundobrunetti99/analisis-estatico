package org.example.cfg;

import java.util.*;

public class ProgramSlicer {
    private final Map<CFGNode, Set<CFGNode>> controlDependencies;
    private final Map<CFGNode, Set<CFGNode>> dataDependencies;

    public ProgramSlicer(Map<CFGNode, Set<CFGNode>> controlDependencies,
                         Map<CFGNode, Set<CFGNode>> dataDependencies) {
        this.controlDependencies = controlDependencies;
        this.dataDependencies = dataDependencies;
    }

    public Set<CFGNode> computeSlice(CFGNode criterion) {
        Set<CFGNode> slice = new LinkedHashSet<>();
        Deque<CFGNode> pending = new ArrayDeque<>();
        pending.add(criterion);

        while (!pending.isEmpty()) {
            CFGNode current = pending.removeFirst();
            if (!slice.add(current)) continue;

            for (CFGNode dependency : controlDependencies.getOrDefault(current, Collections.emptySet())) {
                pending.addLast(dependency);
            }
            for (CFGNode dependency : dataDependencies.getOrDefault(current, Collections.emptySet())) {
                pending.addLast(dependency);
            }
        }

        return slice;
    }
}
