package org.example.cfg;

import java.io.*;
import java.util.*;

/**
 *exporta CFG, arbol de post-dominadores y CDG al formato DOT (Graphviz).
 */
public class DotExporter {

    /*exporta el CFG */
    public static String cfgToDot(List<CFGNode> nodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CFG {\n");
        sb.append("  node [shape=box, fontname=\"Courier\"];\n");

        for (CFGNode n : nodes) {
            String style = "";
            if (n.isEntry) style = ", style=filled, fillcolor=lightblue";
            else if (n.isExit) style = ", style=filled, fillcolor=lightyellow";
            sb.append("  ").append(n.dotId())
              .append(" [label=\"").append(n.dotLabel()).append("\"")
              .append(style).append("];\n");
        }
        sb.append("\n");
        for (CFGNode n : nodes) {
            for (CFGNode succ : n.successors) {
                sb.append("  ").append(n.dotId()).append(" -> ").append(succ.dotId()).append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    /**exporta el arbol de post-dominadores usando los IPD */
    public static String postDomTreeToDot(List<CFGNode> nodes, Map<CFGNode, CFGNode> ipd) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph PostDomTree {\n");
        sb.append("  node [shape=ellipse, fontname=\"Courier\"];\n");
        sb.append("  rankdir=BT;\n");   //bottom-to-top: hijos abajo, raiz arriba

        for (CFGNode n : nodes) {
            String style = "";
            if (n.isEntry) style = ", style=filled, fillcolor=lightblue";
            else if (n.isExit) style = ", style=filled, fillcolor=lightyellow";
            sb.append("  ").append(n.dotId())
              .append(" [label=\"").append(n.dotLabel()).append("\"")
              .append(style).append("];\n");
        }
        sb.append("\n");
        for (Map.Entry<CFGNode, CFGNode> e : ipd.entrySet()) {
            //e.getKey() es post-dominado por e.getValue() (su padre en el arbol)
            sb.append("  ").append(e.getKey().dotId())
              .append(" -> ").append(e.getValue().dotId())
              .append(" [label=\"ipost-dom\"];\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /*exporta el CDG */
    public static String cdgToDot(List<CFGNode> nodes, Map<CFGNode, Set<CFGNode>> cdg) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CDG {\n");
        sb.append("  node [shape=box, fontname=\"Courier\"];\n");

        for (CFGNode n : nodes) {
            String style = "";
            if (n.isEntry) style = ", style=filled, fillcolor=lightblue";
            else if (n.isExit) style = ", style=filled, fillcolor=lightyellow";
            sb.append("  ").append(n.dotId())
              .append(" [label=\"").append(n.dotLabel()).append("\"")
              .append(style).append("];\n");
        }
        sb.append("\n");
        for (Map.Entry<CFGNode, Set<CFGNode>> e : cdg.entrySet()) {
            CFGNode dependent = e.getKey();
            for (CFGNode controller : e.getValue()) {
                sb.append("  ").append(controller.dotId())
                  .append(" -> ").append(dependent.dotId())
                  .append(" [style=dashed, color=red];\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static void writeToFile(String content, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(content);
        }
    }
}
