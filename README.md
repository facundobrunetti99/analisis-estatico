# TP1 - Análisis Estático de Programas

> **Universidad Nacional de Villa Mercedes**  
> Ingeniería en Sistemas de Información · Análisis Estático de Programas 2026

**Integrantes:**  
Omar Rodrigo Villegas · Gonzalo Barroso · Leonelo Benjamín Velasco Monticone · Facundo García Brunetti

---

## ¿Qué hace este proyecto?

Dado un programa escrito en el **mini-lenguaje** definido por el TP, el proyecto construye el AST, genera el CFG y calcula distintos análisis estáticos. Los resultados se exportan como grafos en formato **DOT (Graphviz)**.

| # | Artefacto | Descripción |
|---|-----------|-------------|
| 1 | **CFG** | Control Flow Graph |
| 2 | **Post-dominadores** | Algoritmo iterativo de punto fijo |
| 3 | **Árbol de Post-dominadores** | Construido a partir del IPD |
| 4 | **CDG** | Control Dependence Graph |
| 5 | **Reaching Definitions** | Conjuntos `IN` y `OUT` por nodo |
| 6 | **DDG** | Data Dependence Graph desde pares Definición-Uso |
| 7 | **PDG** | Unión de dependencias de control y de datos |
| 8 | **Program Slice** | Slice hacia atrás sobre CDG + DDG |

---

## Mini-lenguaje soportado

```txt
PROGRAM    -> TYPE ID () { STATEMENTS }
TYPE       -> integer
STATEMENTS -> STATEMENT | STATEMENT STATEMENTS
STATEMENT  -> ID = EXP ;
           | return EXP ;
           | if ( EXP ) { STATEMENTS } else { STATEMENTS }
           | while ( EXP ) { STATEMENTS }
EXP        -> VALUE | VALUE + VALUE
VALUE      -> ID | NUMBER
```

**Ejemplo - `programa.txt` incluido en el proyecto:**

```txt
integer f(){
    x = 3 ;
    if (y) {
        z = x+1;
    }
    else{
        y = x+z;
    }
    return z;
}
```

También se incluye `programa_prueba.txt`, que contiene un caso con `while`, `if/else` y `return`.

---

## Requisitos

- JDK 11 o superior.
- Maven, para compilar el proyecto.
- Graphviz, solo si se quieren convertir los archivos `.dot` a imágenes `.png`.

> El `pom.xml` compila con `maven.compiler.source` y `maven.compiler.target` en Java 11. Si se ejecuta con Java 8, puede aparecer un error de versión de clases (`UnsupportedClassVersionError`).

---

## Estructura del proyecto

```txt
analisis-estatico/
├── pom.xml
├── java-cup-11b.jar
├── java-cup-11b-runtime.jar
├── programa.txt
├── programa_prueba.txt
├── *_cfg.dot
├── *_pdom.dot
├── *_cdg.dot
├── *_ddg.dot
├── *_pdg.dot
├── *_slice_*.dot
└── src/main/java/org/example/
    ├── Main.java
    ├── Lexer.java
    ├── Lexer.java~
    ├── lexer/
    │   └── lexer.flex
    ├── lparser/
    │   ├── grammar.cup
    │   ├── mini.cup
    │   ├── mini.flex
    │   ├── parser.java
    │   └── sym.java
    ├── ast/
    │   ├── ASTNode.java
    │   ├── StatementNode.java
    │   ├── ExpressionNode.java
    │   ├── ProgramNode.java
    │   ├── AssignNode.java
    │   ├── ReturnNode.java
    │   ├── IfNode.java
    │   ├── WhileNode.java
    │   ├── BinaryOpNode.java
    │   ├── VarNode.java
    │   └── NumNode.java
    └── cfg/
        ├── CFGNode.java
        ├── CFGBuilder.java
        ├── CFGCollector.java
        ├── PostDominator.java
        ├── CDGBuilder.java
        ├── ReachingDefinitions.java
        ├── Definition.java
        ├── DDGBuilder.java
        ├── ProgramSlicer.java
        └── DotExporter.java
```

---

## Ejecución paso a paso

### Paso 1 - Generar el parser con CUP

Desde la raíz del proyecto:

```bash
java -jar java-cup-11b.jar \
  -destdir src/main/java/org/example/lparser \
  -package org.example.lparser \
  -parser parser \
  -symbols sym \
  src/main/java/org/example/lparser/grammar.cup
```

### Paso 2 - Compilar con Maven

```bash
mvn clean compile
```

### Paso 3 - Ejecutar

**Windows (PowerShell):**

```powershell
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main programa.txt
```

**Linux / Mac:**

```bash
java -cp "target/classes:java-cup-11b-runtime.jar" org.example.Main programa.txt
```

Para calcular un slice, pasar un segundo argumento como criterio. Puede ser el id del nodo (`B2`), un número de línea o parte del texto de la instrucción:

```powershell
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main programa.txt B2
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main programa.txt 7
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main programa.txt "return z"
```

### Paso 4 - Visualizar los grafos

> Requiere Graphviz instalado. Descargarlo desde [graphviz.org/download](https://graphviz.org/download/) y agregar la carpeta `bin` al `PATH`.

```bash
dot -Tpng programa_cfg.dot  -o programa_cfg.png
dot -Tpng programa_pdom.dot -o programa_pdom.png
dot -Tpng programa_cdg.dot  -o programa_cdg.png
dot -Tpng programa_ddg.dot  -o programa_ddg.png
dot -Tpng programa_pdg.dot  -o programa_pdg.png
dot -Tpng programa_slice_B2.dot -o programa_slice_B2.png
```

---

## Archivos generados

Para una entrada `programa.txt`, la aplicación genera:

| Archivo | Contenido |
|---------|-----------|
| `programa_cfg.dot` | CFG completo |
| `programa_pdom.dot` | Árbol de post-dominadores |
| `programa_cdg.dot` | Dependencias de control |
| `programa_ddg.dot` | Dependencias de datos |
| `programa_pdg.dot` | Grafo de dependencias del programa |
| `programa_slice_<criterio>.dot` | CFG reducido al slice, si se pasa criterio |

El prefijo sale del nombre del archivo de entrada. Por ejemplo, al ejecutar con `programa_prueba.txt` se generan `programa_prueba_cfg.dot`, `programa_prueba_pdom.dot`, `programa_prueba_cdg.dot`, `programa_prueba_ddg.dot`, `programa_prueba_pdg.dot` y, si corresponde, `programa_prueba_slice_<criterio>.dot`.

---

## Salida esperada en consola

```txt
============================================================
TP1 - Analisis Estatico de Programas
Archivo: programa.txt
============================================================

[1] Parseando...
    OK - funcion: f

[2] Generando CFG...
    7 bloques generados

[3] Computando Post-dominadores...
    Tabla de Post-Dominadores:
      PostDom(B0 [ENTRY]) = { ... }

[4] Construyendo Arbol de Post-dominadores...
    Post-dominadores inmediatos (IPD):
      IPD(B...) = B...

[5] Construyendo CDG...
    Dependencias de control:
      ...

[6] Computando Reaching Definitions...
      IN(B...) = { ... }
      OUT(B...) = { ... }

[7] Construyendo DDG...
    Pares Definicion-Uso:
      ...

[8] Calculando Program Slice para B... [...]
    Nodos del slice:
      ...

[9] Exportando archivos DOT...
    CFG  -> programa_cfg.dot
    PDOM -> programa_pdom.dot
    CDG  -> programa_cdg.dot
    DDG  -> programa_ddg.dot
    PDG  -> programa_pdg.dot
    SLICE CFG -> programa_slice_B2.dot
============================================================
```

Si no se pasa criterio de slice, se omite el paso `[8]` y solo se exportan CFG, PDOM, CDG, DDG y PDG.

---

## Probar con otros programas

Crear cualquier archivo `.txt` que respete el mini-lenguaje y pasarlo como primer argumento:

```bash
# Windows
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main mi_programa.txt

# Linux / Mac
java -cp "target/classes:java-cup-11b-runtime.jar" org.example.Main mi_programa.txt
```

---

## Algoritmos implementados

### CFG - Control Flow Graph

El CFG se construye desde el AST. Cada sentencia se transforma en uno o más bloques básicos (`CFGNode`) y se conectan sucesores y predecesores. El `if` genera dos ramas, el `while` genera una arista de vuelta hacia su condición y el `return` conecta con `EXIT`.

### Post-dominadores

Se usa un algoritmo iterativo de punto fijo:

```txt
PostDom(EXIT) = { EXIT }
PostDom(n)    = { n } U intersección(PostDom(s)) para todo sucesor s de n
```

Los nodos distintos de `EXIT` se inicializan con el conjunto de todos los nodos y se itera hasta estabilizar.

### Árbol de Post-dominadores

A partir de la tabla de post-dominadores se calcula el **IPD** (`Immediate Post-Dominator`) de cada nodo. Con esos enlaces se exporta el árbol de post-dominadores.

### CDG - Control Dependence Graph

Para cada arista del CFG donde el sucesor no post-domina al origen, se recorren los IPD para marcar qué nodos quedan controlados por una condición. Además, los nodos sin dependencia de control quedan asociados al nodo `ENTRY`.

### Reaching Definitions

Se calculan conjuntos `GEN`, `KILL`, `IN` y `OUT` por bloque. Una definición alcanza a un nodo si puede llegar a él por algún camino del CFG sin ser sobrescrita por otra definición de la misma variable.

### DDG - Data Dependence Graph

El DDG se construye usando las variables utilizadas por cada nodo y las definiciones que llegan a su conjunto `IN`. Cada arista representa una dependencia de datos entre una definición y un uso, guardando también la variable involucrada.

### PDG y Program Slice

El **PDG** combina las dependencias de control del CDG y las dependencias de datos del DDG. El **Program Slice** toma un criterio y recorre hacia atrás esas dependencias para conservar solo los nodos necesarios. El resultado se exporta como un CFG reducido.
