grammar FeatherweightJavaScript;


@header { package edu.sjsu.fwjs.parser; }

// Reserved words
IF        : 'if' ;
ELSE      : 'else' ;
WHILE      : 'while' ;
FUNCTION   : 'function' ;
VAR        : 'var' ;
PRINT      : 'print' ;

// Literals
INT       : [1-9][0-9]* | '0' ;
BOOL       : 'true' | 'false' ;
NULL       : 'null' ;

// Symbols
MUL       : '*' ;
DIV       : '/' ;
ADD        : '+' ;
SUB        : '-' ;
MOD        : '%' ;

GT         : '>' ;
LT         : '<' ;
GE         : '>=' ;
LE         : '<=' ;
EQ         : '==' ;

ASSIGN     : '=' ;
SEPARATOR : ';' ;

// Identifier: first [A-Za-z_] then [A-Za-z_0-9]*
ID         : [A-Za-z_] [A-Za-z_0-9]* ;

// Whitespace and comments
NEWLINE   : '\r'? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT  : '//' ~[\n\r]* -> skip ;
WS            : [ \t]+ -> skip ; // ignore whitespace

// ***Parsing rules ***

/** The start rule */
prog: stat+ ;

stat: expr SEPARATOR                                    # bareExpr
    | IF '(' expr ')' block ELSE block                  # ifThenElse
    | IF '(' expr ')' block                             # ifThen
    | WHILE '(' expr ')' stat                           # while
    | PRINT '(' expr ')' SEPARATOR                      # print
    | '{' stat* '}'                                     # blockExpr
    ;

block: '{' stat* '}'                                    # fullBlock
     | stat                                             # simpBlock
     ;

expr: '(' expr ')'                                      # parens
    | expr '(' argList? ')'                             # call
    | FUNCTION '(' paramList? ')' block                 # func
    | INT                                               # int
    | BOOL                                              # bool
    | NULL                                              # null
    | ID                                                # id
    | '{' stat* '}'                                     # blockVal
    | expr op=( '*' | '/' | '%' ) expr                  # MulDivMod
    | expr op=('+' | '-') expr                          # AddSub
    | expr op=(LT | LE | GT | GE | EQ) expr             # Compare
    | ID ASSIGN expr                                    # assign
    | VAR ID (ASSIGN expr)?                             # varDecl
    ;

paramList: ID (',' ID)*
    ;

argList: expr (',' expr)*
    ;