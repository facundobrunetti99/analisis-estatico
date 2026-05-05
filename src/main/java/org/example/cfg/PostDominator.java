package org.example.cfg;

import java.util.*;

/**
 *calcula post-dominadores usando el algoritmo iterativo clasico.
 *n post-domina a m  <=>  todo camino de m hacia EXIT pasa por n.
 *ecuacion de punto fijo (sobre el CFG INVERSO):
 *postDom(EXIT) = {EXIT}
 *postDom(n)    = {n} U interseccion( PostDom(s) para cada sucesor s de n )
 */
public class PostDominator {

    private final List<CFGNode> nodes;
    private final CFGNode exit;

    //postDom[n] = conjunto de nodos que post-dominan a n
    private final Map<CFGNode, Set<CFGNode>> postDom = new LinkedHashMap<>();

    public PostDominator(List<CFGNode> nodes, CFGNode exit) {
        this.nodes = nodes;
        this.exit  = exit;
    }

    public void compute() {
        //inicializar: PostDom(EXIT) = {EXIT}, el resto = todos los nodos
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

        //iteracion hasta punto fijo
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CFGNode n : nodes) {
                if (n == exit) continue;

                //interseccion de los post-dominadores de todos los sucesores
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

                //postDom(n) = {n} U intersection
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

    /**retorna el conjunto de nodos que post-dominan a n */
    public Set<CFGNode> getPostDom(CFGNode n) {
        return postDom.getOrDefault(n, Collections.emptySet());
    }

    /**
     *calcula el post-dominador inmediato (IPD) de cada nodo:
     *el post-dominador estricto mas cercano.
     *postDomEst(n) = PostDom(n) - {n}
     *IPD(n) = el nodo m en PostDomEst(n) tal que m in PostDom(p) para todo p in PostDomEst(n)
     */
    public Map<CFGNode, CFGNode> computeIPD() {
    Map<CFGNode, CFGNode> ipd = new LinkedHashMap<>();
    for (CFGNode n : nodes) {
        if (n == exit) continue;

        Set<CFGNode> strict = new LinkedHashSet<>(postDom.get(n));
        strict.remove(n);
        if (strict.isEmpty()) continue;

        // El IPD es el elemento de strict con el PostDom MAS PEQUEÑO
        // (el mas cercano en el arbol = el que post-domina a menos nodos)
        // pero que NO sea EXIT salvo que sea el unico candidato
        CFGNode candidate = null;
        int minSize = Integer.MAX_VALUE;

        for (CFGNode m : strict) {
            if (m == exit) continue; // saltar EXIT en primera pasada
            int size = postDom.getOrDefault(m, Collections.emptySet()).size();
            if (size < minSize) {
                minSize = size;
                candidate = m;
            }
        }

        // si no encontro nada (solo habia EXIT en strict), usar EXIT
        if (candidate == null) candidate = exit;

        ipd.put(n, candidate);
    }
    return ipd;
}

    public Map<CFGNode, Set<CFGNode>> getAllPostDom() { return postDom; }
}
