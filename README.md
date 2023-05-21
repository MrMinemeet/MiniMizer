# MiniMizer
My optimizer for Project 2 of the LVA [Advanced Compiler Construction](https://ssw.jku.at/Teaching/Lectures/ACC/) @JKU Linz by Prof. Hanspeter Mössenböck.


## Task description:
The goal of this project is to write a compiler that translates a program of the toy language Mini
to an intermediate representation (a CFG with IR instructions), performs some optimizations
on it and finally does register allocation by graph coloring. Code generation is not part of this
project. The project can either be implemented in Java or in C#.

## Mini language description:
Mini is a simple Pascal-like language with integer variables and (multi-dimensional) arrays. It
has the usual kinds of statements and expressions. There are no procedures. The syntax of Mini
is as follows:
```
Mini        = "PROGRAM" {VarDecl} "BEGIN" StatSeq "END" "." .
VarDecl     = "VAR" {IdListDecl ";"} .
IdListDecl  = ident {"," ident} ":" Type .
Type        = ident | "ARRAY" number "OF" Type .
StatSeq     = Statement {";" Statement} .
Statement   =
    [ Designator ":=" Expression
    | "IF" Condition "THEN" StatSeq {"ELSIF" Condition "THEN" StatSeq} ["ELSE" StatSeq] "END"
    | "WHILE" Condition "DO" StatSeq "END"
    | "READ" Designator
    | "WRITE" Expression
    ] .
Condition   = Expression Relop Expression .
Expression  = [Addop] Term {Addop Term} .
Term        = Factor {Mulop Factor} .
Factor      = Designator | number | "(" Expression ")" .
Designator  = ident {"[" Expression "]"}.
Relop       = "=" | "#" | "<" | ">" | ">=" | "<=".
Addop       = "+" | "-".
Mulop       = "*" | "/" | "%".
```

The lexical structure of SL is:
```
ident = letter {letter | digit}.
number = digit {digit}
```

Integer types are written as `INTEGER`. Comments start with `//` and go to the end of the line.

---
* Full Task Description: [Project_2.pdf](https://ssw.jku.at/Teaching/Lectures/ACC/Project_2.pdf)
* COCO/R: [Homepage](https://ssw.jku.at/Research/Projects/Coco/)