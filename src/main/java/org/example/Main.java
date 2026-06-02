package org.example;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java_cup.runtime.Symbol;

import org.example.ast.ProgramNode;
import org.example.cfg.*;
import org.example.lparser.parser;
import org.example.lparser.sym;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Uso: java org.example.Main <archivo.txt>");
            System.err.println("  opcional: java org.example.Main <archivo.txt> <nodo|linea>");
            System.err.println("");
            System.err.println("Genera los archivos DOT:");
            System.err.println("  <archivo>_cfg.dot    -> Control Flow Graph");
            System.err.println("  <archivo>_pdom.dot   -> Arbol de Post-dominadores");
            System.err.println("  <archivo>_cdg.dot    -> Control Dependence Graph");
            System.err.println("  <archivo>_ddg.dot    -> Data Dependence Graph");
            System.err.println("  <archivo>_pdg.dot    -> Program Dependence Graph");
            System.err.println("  <archivo>_slice_*.dot -> CFG reducido al slice, si se pide");
            System.exit(1);
        }

        String inputFile = args[0];
        File file = new File(inputFile);
        if (!file.exists()) {
            System.err.println("Error: no existe el archivo '" + inputFile + "'");
            System.exit(1);
        }

        System.out.println(separator());
        System.out.println("TP1 - Analisis Estatico de Programas");
        System.out.println("Archivo: " + inputFile);
        System.out.println(separator());

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

        //Reaching Definitions
        System.out.println("\n[6] Computando Reaching Definitions...");
        ReachingDefinitions reachingDefinitions = new ReachingDefinitions(nodes);
        reachingDefinitions.compute();
        for (CFGNode n : nodes) {
            System.out.println("      IN(" + n.dotId() + " [" + n.dotLabel() + "]) = "
                + formatDefinitions(reachingDefinitions.getIn(n)));
            System.out.println("      OUT(" + n.dotId() + " [" + n.dotLabel() + "]) = "
                + formatDefinitions(reachingDefinitions.getOut(n)));
        }

        //DDG
        System.out.println("\n[7] Construyendo DDG...");
        DDGBuilder ddgBuilder = new DDGBuilder(nodes, reachingDefinitions);
        ddgBuilder.compute();

        System.out.println("    Pares Definicion-Uso:");
        boolean anyDataDep = false;
        for (Map.Entry<CFGNode, Set<CFGNode>> e : ddgBuilder.getAllDDG().entrySet()) {
            CFGNode useNode = e.getKey();
            for (CFGNode defNode : e.getValue()) {
                anyDataDep = true;
                System.out.println("      " + defNode.dotId() + " [" + defNode.dotLabel() + "]  ->  "
                    + useNode.dotId() + " [" + useNode.dotLabel() + "]  vars="
                    + ddgBuilder.getVariables(useNode, defNode));
            }
        }
        if (!anyDataDep) System.out.println("      (ninguna dependencia de datos detectada)");

        //Slicing
        Set<CFGNode> slice = null;
        String sliceFile = null;
        if (args.length >= 2) {
            CFGNode criterion = findCriterionNode(args[1], nodes, file);
            if (criterion == null) {
                System.out.println("\n[8] Slice: no se encontro el criterio '" + args[1] + "'");
            } else {
                System.out.println("\n[8] Calculando Program Slice para "
                    + criterion.dotId() + " [" + criterion.dotLabel() + "]...");
                ProgramSlicer slicer = new ProgramSlicer(cdg, ddgBuilder.getAllDDG());
                slice = slicer.computeSlice(criterion);

                System.out.println("    Nodos del slice:");
                for (CFGNode n : nodes) {
                    if (slice.contains(n) && !n.isEntry && !n.isExit) {
                        System.out.println("      " + n.dotId() + " [" + n.dotLabel() + "]");
                    }
                }
            }
        }

        //Exportar DOT
        System.out.println("\n[9] Exportando archivos DOT...");
        String base = inputFile.replace(".txt", "");

        String cfgFile  = base + "_cfg.dot";
        String pdomFile = base + "_pdom.dot";
        String cdgFile  = base + "_cdg.dot";
        String ddgFile  = base + "_ddg.dot";
        String pdgFile  = base + "_pdg.dot";

        DotExporter.writeToFile(DotExporter.cfgToDot(nodes), cfgFile);
        System.out.println("    CFG  -> " + cfgFile);

        DotExporter.writeToFile(DotExporter.postDomTreeToDot(nodes, ipd), pdomFile);
        System.out.println("    PDOM -> " + pdomFile);

        DotExporter.writeToFile(DotExporter.cdgToDot(nodes, cdg), cdgFile);
        System.out.println("    CDG  -> " + cdgFile);

        DotExporter.writeToFile(DotExporter.ddgToDot(nodes, ddgBuilder), ddgFile);
        System.out.println("    DDG  -> " + ddgFile);

        DotExporter.writeToFile(DotExporter.pdgToDot(nodes, cdg, ddgBuilder), pdgFile);
        System.out.println("    PDG  -> " + pdgFile);

        if (slice != null) {
            sliceFile = base + "_slice_" + sanitizeFilename(args[1]) + ".dot";
            DotExporter.writeToFile(DotExporter.sliceCfgToDot(nodes, slice), sliceFile);
            System.out.println("    SLICE CFG -> " + sliceFile);
        }

        System.out.println("\n" + separator());
        System.out.println("Para visualizar los grafos instala Graphviz y ejecuta:");
        System.out.println("  dot -Tpng " + cfgFile  + " -o " + base + "_cfg.png");
        System.out.println("  dot -Tpng " + pdomFile + " -o " + base + "_pdom.png");
        System.out.println("  dot -Tpng " + cdgFile  + " -o " + base + "_cdg.png");
        System.out.println("  dot -Tpng " + ddgFile  + " -o " + base + "_ddg.png");
        System.out.println("  dot -Tpng " + pdgFile  + " -o " + base + "_pdg.png");
        if (sliceFile != null) {
            System.out.println("  dot -Tpng " + sliceFile + " -o " + sliceFile.replace(".dot", ".png"));
        }
        System.out.println(separator());
    }

    private static String separator() {
        char[] chars = new char[60];
        Arrays.fill(chars, '=');
        return new String(chars);
    }

    private static String formatDefinitions(Set<Definition> definitions) {
        if (definitions.isEmpty()) return "{ }";
        StringBuilder sb = new StringBuilder("{ ");
        for (Definition d : definitions) {
            sb.append(d.variable)
              .append("@")
              .append(d.node.dotId())
              .append(" ");
        }
        sb.append("}");
        return sb.toString();
    }

    private static CFGNode findCriterionNode(String criterion, List<CFGNode> nodes, File inputFile) throws IOException {
        String text = criterion.trim();
        if (text.matches("[Bb]\\d+")) {
            return findById(Integer.parseInt(text.substring(1)), nodes);
        }
        if (text.matches("\\d+")) {
            CFGNode byLine = findBySourceLine(Integer.parseInt(text), nodes, inputFile);
            return byLine != null ? byLine : findById(Integer.parseInt(text), nodes);
        }

        String normalized = normalize(text);
        for (CFGNode n : nodes) {
            if (normalize(n.dotLabel()).equals(normalized)) return n;
        }
        for (CFGNode n : nodes) {
            if (normalize(n.dotLabel()).contains(normalized)
                || normalized.contains(normalize(n.dotLabel()))) {
                return n;
            }
        }
        return null;
    }

    private static CFGNode findById(int id, List<CFGNode> nodes) {
        for (CFGNode n : nodes) {
            if (n.id == id) return n;
        }
        return null;
    }

    private static CFGNode findBySourceLine(int lineNumber, List<CFGNode> nodes, File inputFile) throws IOException {
        List<String> lines = Files.readAllLines(inputFile.toPath());
        if (lineNumber < 1 || lineNumber > lines.size()) return null;

        String line = normalize(lines.get(lineNumber - 1));
        if (line.isEmpty()) return null;

        for (CFGNode n : nodes) {
            if (n.isEntry || n.isExit) continue;
            String label = normalize(n.dotLabel());
            if (line.contains(label) || label.contains(line)) {
                return n;
            }
        }
        return null;
    }

    private static String normalize(String text) {
        return text.toLowerCase()
            .replace(" ", "")
            .replace("\t", "")
            .replace(";", "")
            .replace("{", "")
            .replace("}", "");
    }

    private static String sanitizeFilename(String text) {
        return text.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
