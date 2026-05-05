package org.example.cfg;

import java.util.*;

public class CDGBuilder {

    private final List<CFGNode> nodes;
    private final PostDominator pd;
    private final Map<CFGNode, CFGNode> ipd;

    //cdg[n] = conjunto de nodos de los que n es control-dependiente
    private final Map<CFGNode, Set<CFGNode>> cdg = new LinkedHashMap<>();

    public CDGBuilder(List<CFGNode> nodes, PostDominator pd) {
        this.nodes = nodes;
        this.pd    = pd;
        this.ipd   = pd.computeIPD();
        for (CFGNode n : nodes) cdg.put(n, new LinkedHashSet<>());
    }

    public void compute() {
        for (CFGNode a : nodes) {
            for (CFGNode b : a.successors) {
                //si B post-domina a A, esta arista no genera dependencias
                if (pd.getPostDom(a).contains(b)) continue;

                //subir desde A hasta el IPD de A marcando dependencias
                CFGNode current = b;
                CFGNode limit   = ipd.get(a); //padre de A en arbol post-dom

                while (current != null && current != limit) {
                    cdg.get(current).add(a);
                    current = ipd.get(current);
                }
            }
        }
    }

    /**retorna el conjunto de nodos de los que n depende en control */
    public Set<CFGNode> getDependenciesOf(CFGNode n) {
        return cdg.getOrDefault(n, Collections.emptySet());
    }

    public Map<CFGNode, Set<CFGNode>> getAllCDG() { return cdg; }
}
