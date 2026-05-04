package org.example;
import java_cup.runtime.*;
import org.example.lparser.sym;

%%

%class Lexer
%line
%column
%cup

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = "//" [^\r\n]*
dec_int_lit    = 0 | [1-9][0-9]*
identifier     = [A-Za-z_][A-Za-z_0-9]*

%%

<YYINITIAL> {
    "integer"          { return symbol(sym.INTEGER); }
    "return"           { return symbol(sym.RETURN); }
    "if"               { return symbol(sym.IF); }
    "else"             { return symbol(sym.ELSE); }
    "while"            { return symbol(sym.WHILE); }

    "="                { return symbol(sym.ASSIGN); }
    "+"                { return symbol(sym.PLUS); }
    "("                { return symbol(sym.LPAREN); }
    ")"                { return symbol(sym.RPAREN); }
    "{"                { return symbol(sym.LBRACE); }
    "}"                { return symbol(sym.RBRACE); }
    ";"                { return symbol(sym.SEMI); }

    {dec_int_lit}      { return symbol(sym.NUMBER, Integer.parseInt(yytext())); }
    {identifier}       { return symbol(sym.ID, yytext()); }
    {WhiteSpace}       { /* ignorar */ }
    {Comment}          { /* ignorar */ }
}

[^]  { throw new Error("Caracter ilegal: <" + yytext() + "> en linea " + (yyline+1)); }
