# TP1 — Análisis Estático de Programas

> **Universidad Nacional de Villa Mercedes**  
> Ingeniería en Sistemas de Información · Análisis Estático de Programas 2026

**Integrantes:**
Omar Rodrigo Villegas · Gonzalo Barroso · Leonelo Benjamín Velasco Monticone · Facundo Garcia Brunetti

---

## ¿Qué hace este proyecto?

Dado un programa en el **mini-lenguaje** definido por el TP, genera automáticamente los siguientes análisis estáticos y los exporta como grafos visualizables:

| # | Artefacto | Descripción |
|---|-----------|-------------|
| 1 | **CFG** | Control Flow Graph |
| 2 | **Post-dominadores** | Algoritmo iterativo de punto fijo |
| 3 | **Árbol de Post-dominadores** | Usando IPD (post-dominador inmediato) |
| 4 | **CDG** | Control Dependence Graph |

Los grafos se exportan en formato **DOT (Graphviz)** y se pueden convertir a imágenes PNG.

---

## Mini-lenguaje soportado

```
PROGRAM    → TYPE ID () { STATEMENTS }
TYPE       → integer
STATEMENTS → STATEMENT | STATEMENT STATEMENTS
STATEMENT  → ID = EXP ;
           | return EXP ;
           | if ( EXP ) { STATEMENTS } else { STATEMENTS }
           | while ( EXP ) { STATEMENTS }
EXP        → VALUE | VALUE + VALUE
VALUE      → ID | NUMBER
```

**Ejemplo — `programa.txt` (incluido en el proyecto):**

```
integer f(){
    x = 3 ;
    if (y) {
        z = x+1;
    }
    else {
        y = x+z;
    }
    return z;
}
```

---

## Estructura del proyecto

```
TP1_AEP/
├── pom.xml
├── programa.txt                           ← programa de prueba
├── java-cup-11b.jar                       ← CUP (generador de parser)
├── java-cup-11b-runtime.jar               ← runtime de CUP
└── src/main/java/org/example/
    ├── Main.java                          ← punto de entrada
    ├── Lexer.java                         ← generado por JFlex
    ├── lexer/
    │   └── lexer.flex                     ← especificación del lexer
    ├── lparser/
    │   ├── grammar.cup                    ← gramática CUP
    │   ├── parser.java                    ← generado por CUP
    │   └── sym.java                       ← generado por CUP
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
        ├── CFGNode.java                   ← bloque básico
        ├── CFGBuilder.java                ← construcción del CFG
        ├── CFGCollector.java              ← BFS para recolectar nodos
        ├── PostDominator.java             ← post-dominadores e IPD
        ├── CDGBuilder.java                ← construcción del CDG
        └── DotExporter.java               ← exportación a formato DOT
```

---

## Ejecución paso a paso

### Paso 1 — Generar el parser con CUP

Desde la raíz del proyecto (`/TP1_AEP/`):

```bash
java -jar java-cup-11b.jar \
  -destdir src/main/java/org/example/lparser \
  -package org.example.lparser \
  -parser parser \
  -symbols sym \
  src/main/java/org/example/lparser/grammar.cup
```

### Paso 2 — Compilar con Maven

```bash
mvn clean compile
```

### Paso 3 — Ejecutar

**Windows (PowerShell):**
```powershell
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main programa.txt
```

**Linux / Mac:**
```bash
java -cp "target/classes:java-cup-11b-runtime.jar" org.example.Main programa.txt
```

### Paso 4 — Visualizar los grafos

> **Requiere Graphviz instalado.** Descargarlo desde [graphviz.org/download](https://graphviz.org/download/)  
> Una vez instalado, agregar la carpeta `bin` al PATH del sistema.  
> Ejemplo Windows: `D:\Graphviz-14.1.5-win64\bin`

```bash
dot -Tpng programa_cfg.dot  -o programa_cfg.png
dot -Tpng programa_pdom.dot -o programa_pdom.png
dot -Tpng programa_cdg.dot  -o programa_cdg.png
```

---

## Salida esperada en consola

```
============================================================
 TP1 - Análisis Estático de Programas
 Archivo: programa.txt
============================================================

[1] Parseando...
    OK — función: f

[2] Generando CFG...
    7 bloques generados

[3] Computando Post-dominadores...
    Tabla de Post-Dominadores:
      PostDom(B0 [ENTRY]) = { B0 B6 }
      ...

[4] Construyendo Árbol de Post-dominadores...
    Post-dominadores inmediatos (IPD):
      IPD(B1 [x = 3]) = B6 [EXIT]
      ...

[5] Construyendo CDG...
    Dependencias de control:
      B3 [z = x + 1]  depende de  B2 [if (y)]
      ...

[6] Exportando archivos DOT...
    CFG  → programa_cfg.dot
    PDOM → programa_pdom.dot
    CDG  → programa_cdg.dot
============================================================
```

---

## Probar con otros programas

Crear cualquier archivo `.txt` respetando el mini-lenguaje y pasarlo como argumento:

```bash
# Windows
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main mi_programa.txt

# Linux / Mac
java -cp "target/classes:java-cup-11b-runtime.jar" org.example.Main mi_programa.txt
```

---

## Algoritmos implementados

### CFG — Control Flow Graph

Construcción directa desde el AST usando el patrón **continuation-passing**: cada sentencia recibe el bloque siguiente (`next`) como parámetro y retorna su primer bloque. El `if` genera dos sucesores (rama `T` y rama `F`); el `while` crea un ciclo hacia el nodo de condición.

### Post-dominadores

Algoritmo iterativo de **punto fijo** sobre el CFG:

```
PostDom(EXIT) = {EXIT}
PostDom(n)    = {n} ∪ ⋂ PostDom(s)   para todo sucesor s de n
```

Se inicializa `PostDom(n) = todos los nodos` para `n ≠ EXIT` y se itera hasta estabilizarse.

### Árbol de Post-dominadores (IPD)

El **IPD** (post-dominador inmediato) de `n` es el único elemento de `PostDomEst(n)` que es post-dominado por todos los demás elementos de ese conjunto. Forma el árbol con `EXIT` en la raíz.

### CDG — Control Dependence Graph

Para cada arista `(A → B)` del CFG donde `B` **no** post-domina a `A`: se sube desde `B` por el árbol de post-dominadores hasta `IPD(A)`, marcando cada nodo visitado como **control-dependiente de A**.
