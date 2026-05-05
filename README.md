# TP1 - Analisis Estatico de Programas
**Universidad Nacional de Villa Mercedes**  
Ingenieria en Sistemas de Informacion - Analisis Estatico de Programas 2026

---

## Que hace este proyecto

Dado un programa en el **mini-lenguaje** definido por el TP, genera automaticamente:

1. **CFG** - Control Flow Graph
2. **Post-dominadores** (algoritmo iterativo de punto fijo)
3. **Arbol de Post-dominadores** (usando IPD - post-dominador inmediato)
4. **CDG** - Control Dependence Graph

Los grafos se exportan en formato **DOT (Graphviz)**.

---

## Mini-lenguaje soportado

```
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

**Ejemplo (programa.txt - incluido en el proyecto):**
```
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

---

## Estructura del proyecto

```
TP1_AEP/
├── pom.xml
├── programa.txt                          <- programa de prueba (ejemplo del TP)
├── java-cup-11b.jar                      <- CUP (generador de parser)
├── java-cup-11b-runtime.jar              <- Runtime de CUP
└── src/main/java/org/example/
    ├── Main.java                         <- Punto de entrada
    ├── Lexer.java                        <- (generado por JFlex, NO editar)
    ├── lexer/
    │   └── lexer.flex                    <- Especificacion del lexer (JFlex)
    ├── lparser/
    │   ├── grammar.cup                   <- Gramatica (CUP)
    │   ├── parser.java                   <- (generado por CUP, NO editar)
    │   └── sym.java                      <- (generado por CUP, NO editar)
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
        ├── CFGNode.java                  <- Bloque basico
        ├── CFGBuilder.java               <- Construccion del CFG
        ├── CFGCollector.java             <- BFS para recolectar nodos
        ├── PostDominator.java            <- Calculo de post-dominadores e IPD
        ├── CDGBuilder.java               <- Construccion del CDG
        └── DotExporter.java              <- Exportacion a formato DOT
```

---

## EJECUCION (paso a paso)

### Paso 1: Generar el parser con CUP

Desde la raiz del proyecto `/TP1_AEP/`:

```
java -jar java-cup-11b.jar -destdir src/main/java/org/example/lparser -package org.example.lparser -parser parser -symbols sym src/main/java/org/example/lparser/grammar.cup
```

### Paso 2: Compilar con Maven

```
mvn clean compile
```

### Paso 3: Ejecutar

```
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main programa.txt
```

*(En Linux/Mac usar `:` en lugar de `;` en el classpath)*

```
java -cp "target/classes:java-cup-11b-runtime.jar" org.example.Main programa.txt
```

### Paso 4: Visualizar los grafos (requiere Graphviz instalado)
### Instalarlo desde aqui-> https://graphviz.org/download/
## Una vez instalado agregarlo al PATH D:\Graphviz-14.1.5-win64\bin (Buscar la ruta donde se encuentre)
## Ejecutando estos comandos se generara unos archivos png, donde mostraran graficamente 
```
dot -Tpng programa_cfg.dot  -o programa_cfg.png
dot -Tpng programa_pdom.dot -o programa_pdom.png
dot -Tpng programa_cdg.dot  -o programa_cdg.png
```
## Salida esperada (consola)

```
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
      PostDom(B0 [ENTRY]) = { B0 B6 }
      ...

[4] Construyendo Arbol de Post-dominadores...
    Post-dominadores inmediatos (IPD):
      IPD(B1 [x = 3]) = B6 [EXIT]
      ...

[5] Construyendo CDG...
    Dependencias de control:
      B3 [z = x + 1]  depende de  B2 [if (y)]
      ...

[6] Exportando archivos DOT...
    CFG  -> programa_cfg.dot
    PDOM -> programa_pdom.dot
    CDG  -> programa_cdg.dot
============================================================
```

---

## Probar con otros programas

Crea cualquier `.txt` con el mini-lenguaje y pasalo como argumento:

```
java -cp "target/classes;java-cup-11b-runtime.jar" org.example.Main mi_programa.txt
```

---

## Algoritmos implementados

### CFG
Construccion directa desde el AST usando el patron *continuation-passing*:
cada sentencia recibe el bloque siguiente (`next`) y retorna su primer bloque.

### Post-dominadores
Algoritmo iterativo de punto fijo sobre el CFG:
- `PostDom(EXIT) = {EXIT}`
- `PostDom(n) = {n} U interseccion(PostDom(s) para todo s sucesor de n)`

### Arbol de Post-dominadores
El **IPD** (post-dominador inmediato) de `n` es el unico elemento de
`PostDomEst(n)` que es post-dominado por todos los demas elementos de `PostDomEst(n)`.

### CDG
Para cada arista `(A -> B)` del CFG donde `B` no post-domina a `A`:
se suben los nodos desde `B` hasta el `IPD(A)` en el arbol de post-dominadores,
y cada uno se marca como control-dependiente de `A`.
