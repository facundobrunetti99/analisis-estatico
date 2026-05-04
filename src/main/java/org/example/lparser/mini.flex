package org.example.lparser;
import java_cup.runtime.*;
import org.example.lparser.sym;

%%

%class Lexer
%line
%column
%cup
%unicode

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
Number         = 0 | [1-9][0-9]*
Identifier     = [A-Za-z_][A-Za-z_0-9]*
Comment        = "//" [^\r\n]*

%%

<YYINITIAL> {
    "integer"          { return symbol(sym.INTEGER); }
    "return"           { return symbol(sym.RETURN); }
    "if"               { return symbol(sym.IF); }
    "else"             { return symbol(sym.ELSE); }
    "while"            { return symbol(sym.WHILE); }

    ";"                { return symbol(sym.SEMI); }
    "+"                { return symbol(sym.PLUS); }
    "="                { return symbol(sym.ASSIGN); }
    "("                { return symbol(sym.LPAREN); }
    ")"                { return symbol(sym.RPAREN); }
    "{"                { return symbol(sym.LBRACE); }
    "}"                { return symbol(sym.RBRACE); }

    {Number}           { return symbol(sym.NUMBER, Integer.parseInt(yytext())); }
    {Identifier}       { return symbol(sym.ID, yytext()); }
    {WhiteSpace}       { /* ignorar */ }
    {Comment}          { /* ignorar comentarios */ }
}

[^]  { throw new Error("Caracter invalido: <" + yytext() + "> en linea " + (yyline+1)); }
