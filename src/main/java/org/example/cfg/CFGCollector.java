package org.example.cfg;

import java.util.*;

/**
 *recolecta todos los nodos del CFG haciendo BFS desde ENTRY.
 */
public class CFGCollector {

    public static List<CFGNode> collect(CFGNode entry) {
        List<CFGNode> result  = new ArrayList<>();
        Set<CFGNode>  visited = new LinkedHashSet<>();
        Queue<CFGNode> queue  = new LinkedList<>();

        queue.add(entry);
        visited.add(entry);

        while (!queue.isEmpty()) {
            CFGNode n = queue.poll();
            result.add(n);
            for (CFGNode succ : n.successors) {
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    queue.add(succ);
                }
            }
        }
        return result;
    }
}
