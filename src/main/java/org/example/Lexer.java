package org.example;

import java_cup.runtime.*;
import org.example.lparser.sym;
import java.io.*;

/**
 * Lexer escrito a mano para el mini-lenguaje del TP1.
 * Reconoce: integer, return, if, else, while, ID, NUMBER, =, +, (, ), {, }, ;
 */
public class Lexer implements java_cup.runtime.Scanner {

    private final Reader reader;
    private int current = -2;  // -2 = no leido aun
    private int line   = 0;
    private int column = 0;

    public Lexer(Reader reader) {
        this.reader = reader;
    }

    private int peek() throws IOException {
        if (current == -2) current = reader.read();
        return current;
    }

    private int consume() throws IOException {
        int c = peek();
        current = -2;
        if (c == '\n') { line++; column = 0; }
        else { column++; }
        return c;
    }

    private Symbol sym(int type) {
        return new Symbol(type, line, column);
    }

    private Symbol sym(int type, Object val) {
        return new Symbol(type, line, column, val);
    }

    @Override
    public Symbol next_token() throws Exception {
        // Saltar blancos y comentarios
        while (true) {
            int c = peek();
            if (c == -1) return sym(sym.EOF);

            if (Character.isWhitespace(c)) { consume(); continue; }

            // Comentario de linea //
            if (c == '/') {
                consume();
                int c2 = peek();
                if (c2 == '/') {
                    consume();
                    while (peek() != '\n' && peek() != -1) consume();
                    continue;
                }
                throw new Error("Caracter ilegal: / en linea " + (line+1));
            }

            break;
        }

        int c = peek();
        if (c == -1) return sym(sym.EOF);

        // Simbolos de un caracter
        switch (c) {
            case '=': consume(); return sym(sym.ASSIGN);
            case '+': consume(); return sym(sym.PLUS);
            case '(': consume(); return sym(sym.LPAREN);
            case ')': consume(); return sym(sym.RPAREN);
            case '{': consume(); return sym(sym.LBRACE);
            case '}': consume(); return sym(sym.RBRACE);
            case ';': consume(); return sym(sym.SEMI);
        }

        // Numeros
        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            while (Character.isDigit(peek())) sb.append((char) consume());
            return sym(sym.NUMBER, Integer.parseInt(sb.toString()));
        }

        // Identificadores y palabras clave
        if (Character.isLetter(c) || c == '_') {
            StringBuilder sb = new StringBuilder();
            while (Character.isLetterOrDigit(peek()) || peek() == '_')
                sb.append((char) consume());
            String word = sb.toString();
            switch (word) {
                case "integer": return sym(sym.INTEGER);
                case "return":  return sym(sym.RETURN);
                case "if":      return sym(sym.IF);
                case "else":    return sym(sym.ELSE);
                case "while":   return sym(sym.WHILE);
                default:        return sym(sym.ID, word);
            }
        }

        throw new Error("Caracter ilegal: <" + (char)c + "> en linea " + (line+1));
    }
}
