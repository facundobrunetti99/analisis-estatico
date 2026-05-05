package org.example;

import java.io.*;
import java.util.*;
import java_cup.runtime.Symbol;

import org.example.ast.ProgramNode;
import org.example.cfg.*;
import org.example.lparser.parser;
import org.example.lparser.sym;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Uso: java org.example.Main <archivo.txt>");
            System.err.println("");
            System.err.println("Genera los archivos DOT:");
            System.err.println("  <archivo>_cfg.dot    -> Control Flow Graph");
            System.err.println("  <archivo>_pdom.dot   -> Arbol de Post-dominadores");
            System.err.println("  <archivo>_cdg.dot    -> Control Dependence Graph");
            System.exit(1);
        }

        String inputFile = args[0];
        File file = new File(inputFile);
        if (!file.exists()) {
            System.err.println("Error: no existe el archivo '" + inputFile + "'");
            System.exit(1);
        }

        System.out.println("=".repeat(60));
        System.out.println("TP1 - Analisis Estatico de Programas");
        System.out.println("Archivo: " + inputFile);
        System.out.println("=".repeat(60));

        //Parsing
        System.out.println("\n[1] Parseando...");
        FileReader reader = new FileReader(file);
        Lexer lexer = new Lexer(reader);
        parser p = new parser(lexer);
        Symbol result = p.parse();
        ProgramNode program = (ProgramNode) result.value;
        System.out.println("    OK - funcion: " + program.functionName);

        //CFG
        System.out.println("\n[2] Generando CFG...");
        CFGBuilder builder = new CFGBuilder();
        builder.build(program);
        CFGNode entry = builder.getEntry();
        CFGNode exit  = builder.getExit();

        List<CFGNode> nodes = CFGCollector.collect(entry);
        System.out.println("    " + nodes.size() + " bloques generados");

        //Post-dominadores
        System.out.println("\n[3] Computando Post-dominadores...");
        PostDominator pd = new PostDominator(nodes, exit);
        pd.compute();

        //Mostrar tabla de post-dominadores
        System.out.println("    Tabla de Post-Dominadores:");
        for (CFGNode n : nodes) {
            System.out.print("      PostDom(" + n.dotId() + " [" + n.dotLabel() + "]) = { ");
            for (CFGNode dom : pd.getPostDom(n)) {
                System.out.print(dom.dotId() + " ");
            }
            System.out.println("}");
        }

        //Arbol de post-dominadores 
        System.out.println("\n[4] Construyendo Arbol de Post-dominadores...");
        Map<CFGNode, CFGNode> ipd = pd.computeIPD();
        System.out.println("    Post-dominadores inmediatos (IPD):");
        for (Map.Entry<CFGNode, CFGNode> e : ipd.entrySet()) {
            System.out.println("      IPD(" + e.getKey().dotId() + " [" + e.getKey().dotLabel() + "]) = "
                + e.getValue().dotId() + " [" + e.getValue().dotLabel() + "]");
        }

        //CDG
        System.out.println("\n[5] Construyendo CDG...");
        CDGBuilder cdgBuilder = new CDGBuilder(nodes, pd);
        cdgBuilder.compute();
        Map<CFGNode, Set<CFGNode>> cdg = cdgBuilder.getAllCDG();

        System.out.println("    Dependencias de control:");
        boolean anyDep = false;
        for (Map.Entry<CFGNode, Set<CFGNode>> e : cdg.entrySet()) {
            if (!e.getValue().isEmpty()) {
                anyDep = true;
                for (CFGNode ctrl : e.getValue()) {
                    System.out.println("      " + e.getKey().dotId() + " [" + e.getKey().dotLabel()
                        + "]  depende de  " + ctrl.dotId() + " [" + ctrl.dotLabel() + "]");
                }
            }
        }
        if (!anyDep) System.out.println("      (ninguna dependencia de control detectada)");

        //Exportar DOT
        System.out.println("\n[6] Exportando archivos DOT...");
        String base = inputFile.replace(".txt", "");

        String cfgFile  = base + "_cfg.dot";
        String pdomFile = base + "_pdom.dot";
        String cdgFile  = base + "_cdg.dot";

        DotExporter.writeToFile(DotExporter.cfgToDot(nodes), cfgFile);
        System.out.println("    CFG  -> " + cfgFile);

        DotExporter.writeToFile(DotExporter.postDomTreeToDot(nodes, ipd), pdomFile);
        System.out.println("    PDOM -> " + pdomFile);

        DotExporter.writeToFile(DotExporter.cdgToDot(nodes, cdg), cdgFile);
        System.out.println("    CDG  -> " + cdgFile);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Para visualizar los grafos instala Graphviz y ejecuta:");
        System.out.println("  dot -Tpng " + cfgFile  + " -o " + base + "_cfg.png");
        System.out.println("  dot -Tpng " + pdomFile + " -o " + base + "_pdom.png");
        System.out.println("  dot -Tpng " + cdgFile  + " -o " + base + "_cdg.png");
        System.out.println("=".repeat(60));
    }
}
