package org.example.cfg;

import java.util.*;

/**
 * Calcula post-dominadores usando el algoritmo iterativo clasico.
 *
 * n post-domina a m  <=>  todo camino de m hacia EXIT pasa por n.
 *
 * Ecuacion de punto fijo (sobre el CFG INVERSO):
 *   PostDom(EXIT) = {EXIT}
 *   PostDom(n)    = {n} U interseccion( PostDom(s) para cada sucesor s de n )
 */
public class PostDominator {

    private final List<CFGNode> nodes;
    private final CFGNode exit;

    // postDom[n] = conjunto de nodos que post-dominan a n
    private final Map<CFGNode, Set<CFGNode>> postDom = new LinkedHashMap<>();

    public PostDominator(List<CFGNode> nodes, CFGNode exit) {
        this.nodes = nodes;
        this.exit  = exit;
    }

    public void compute() {
        // Inicializar: PostDom(EXIT) = {EXIT}, el resto = todos los nodos
        Set<CFGNode> allNodes = new LinkedHashSet<>(nodes);

        for (CFGNode n : nodes) {
            if (n == exit) {
                Set<CFGNode> s = new LinkedHashSet<>();
                s.add(exit);
                postDom.put(n, s);
            } else {
                postDom.put(n, new LinkedHashSet<>(allNodes));
            }
        }

        // Iteracion hasta punto fijo
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CFGNode n : nodes) {
                if (n == exit) continue;

                // interseccion de los post-dominadores de todos los sucesores
                Set<CFGNode> intersection = null;
                for (CFGNode succ : n.successors) {
                    Set<CFGNode> pd = postDom.get(succ);
                    if (pd == null) continue;
                    if (intersection == null) {
                        intersection = new LinkedHashSet<>(pd);
                    } else {
                        intersection.retainAll(pd);
                    }
                }
                if (intersection == null) intersection = new LinkedHashSet<>();

                // PostDom(n) = {n} U intersection
                Set<CFGNode> newPD = new LinkedHashSet<>();
                newPD.add(n);
                newPD.addAll(intersection);

                if (!newPD.equals(postDom.get(n))) {
                    postDom.put(n, newPD);
                    changed = true;
                }
            }
        }
    }

    /** Retorna el conjunto de nodos que post-dominan a n */
    public Set<CFGNode> getPostDom(CFGNode n) {
        return postDom.getOrDefault(n, Collections.emptySet());
    }

    /**
     * Calcula el post-dominador inmediato (IPD) de cada nodo:
     * el post-dominador estricto mas cercano.
     *
     * PostDomEst(n) = PostDom(n) - {n}
     * IPD(n) = el nodo m en PostDomEst(n) tal que m in PostDom(p) para todo p in PostDomEst(n)
     */
    public Map<CFGNode, CFGNode> computeIPD() {
        Map<CFGNode, CFGNode> ipd = new LinkedHashMap<>();
        for (CFGNode n : nodes) {
            if (n == exit) continue;
            Set<CFGNode> strict = new LinkedHashSet<>(postDom.get(n));
            strict.remove(n);
            if (strict.isEmpty()) continue;

            // El IPD es el nodo en strict que es post-dominado por todos los demas
            CFGNode candidate = null;
            for (CFGNode m : strict) {
                boolean dominated = true;
                for (CFGNode p : strict) {
                    if (p == m) continue;
                    if (!postDom.get(p).contains(m)) { dominated = false; break; }
                }
                if (dominated) { candidate = m; break; }
            }
            if (candidate != null) ipd.put(n, candidate);
        }
        return ipd;
    }

    public Map<CFGNode, Set<CFGNode>> getAllPostDom() { return postDom; }
}
