// this is the grammar to defines LPHY language.
// It contains 4 scenarios: either, or both a data and model block,
// and free lines without data and model blocks.
grammar LPhy;

// This is the top-level rule that the parser will start with.
input: structured_input | free_lines;
// Define a rule called input that matches an optional data block and an optional model block.
structured_input: datablock? modelblock?;
// free lines without data and model blocks.
free_lines: | relation_list ;


// *** data block *** //
// A rule defines data block that matches the keyword DATA and { } with an optional determ_relation_list in between,
// or no keyword DATA and { }.
datablock: DATA '{' determ_relation_list? '}' | determ_relation_list;

// a rule called determ_relation_list that matches either a determ_relation_line or a determ_relation_list followed by a determ_relation_line.
determ_relation_list:	determ_relation_line | determ_relation_list determ_relation_line;

// a rule called determ_relation_line that matches a determ_relation followed by a semicolon.
determ_relation_line: determ_relation ';';

// *** model block *** //
// A rule defines model block that matches the keyword MODEL and { } with an optional relation_list in between,
// or no keyword MODEL and { }.
modelblock: MODEL '{' relation_list? '}' | relation_list;

// a rule called relation_list that matches either a relation or a relation_list followed by a relation.
relation_list:	relation | relation_list relation;

// a rule called relation that matches either a stoch_relation or a determ_relation,
// each followed by a semicolon.
relation: stoch_relation ';' | determ_relation ';';

// *** assign relations *** //
// a rule called var that matches either a NAME or a NAME followed by a pair of square brackets containing a range_list.
var: NAME | NAME '[' range_list ']';

// a rule called range_list which specifies a comma-separated list of expressions.
// (',' expression)* is a zero or more repetition of the pattern ", expression".
// This means that after the first expression, any number of additional expressions can follow,
// each separated by a comma.
range_list : expression (',' expression)*  ;

// a rule called determ_relation that matches a var followed by an assignment followed by an expression.
determ_relation: var ASSIGN expression ;

// a rule called stoch_relation that matches a var followed by a tilde (~) followed by a distribution.
stoch_relation:	var TILDE distribution ;

// Define a constant which can be a variety of literals (e.g. floats, decimals, strings, booleans)
// optionally preceded by a minus sign.
constant : '-'? (FLOAT_LITERAL|DECIMAL_LITERAL|OCT_LITERAL|HEX_LITERAL|HEX_FLOAT_LITERAL|STRING_LITERAL|'true'|'false');

// An expression_list consists of one or more named_expressions separated by commas.
expression_list : named_expression (',' named_expression)*  ;

// An unnamed_expression_list consists of one or more expressions separated by commas.
unnamed_expression_list : expression (',' expression)*  ;

// Define a mapFunction which takes an expression_list as an argument and is enclosed in curly braces.
mapFunction: '{' expression_list '}';

// Defines a methodCall which consists of a method name (NAME)
// followed by an optional expression_list enclosed in parentheses
// or an optional unnamed_expression_list enclosed in parentheses.
methodCall
    : NAME '(' expression_list? ')'
    | NAME '(' unnamed_expression_list? ')'
    ;

// a methodCall applied to an object (var) separated by a dot (.).
objectMethodCall : var DOT NAME '(' unnamed_expression_list? ')';

// a distribution function which takes an expression_list as an argument.
distribution : NAME '(' expression_list ')' ;

// a named_expression which consists of a variable name (NAME) followed by an equal sign and an expression.
named_expression : NAME ASSIGN expression ;

// rule for array expression
array_expression : '[' unnamed_expression_list? ']' ;

// define an expression which can be a constant, variable name, a parenthesized expression,
// a list of unnamed expressions enclosed in square brackets, a method call, an object method call,
// an increment/decrement operation, a prefix operation, a binary operation, a ternary operation,
// or a mapFunction.
expression
    : constant
    | NAME
    | '(' expression ')'
    | array_expression
    | expression '[' range_list ']'
    | methodCall
    | objectMethodCall
    | expression postfix=('++' | '--')
    | prefix=('+'|'-'|'++'|'--') expression
//    | prefix=('~'|'!') expression
//    | prefix=('!') expression
    | expression bop=('**'|'*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=('==' | '!=') expression
    | expression bop='&' expression
    | expression bop='^' expression
    | expression bop='|' expression
    | expression bop='&&' expression
    | expression bop='||' expression
    | expression bop=':' expression
    | mapFunction
//    | expression bop='?' expression ':' expression
//    | <assoc=right> expression
//      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
//	  expression
;

// Identifiers
DATA:               'data';
MODEL:              'model';
ASSIGN:             '=';
TILDE:              '~';
DOT:                '.';

NAME:               Letter LetterOrDigit*;

LENGTH:             'length';
DIM:                'dim';

/*
 * Adapted from https://github.com/antlr/grammars-v4/blob/master/java/JavaLexer.g4
 [The "BSD licence"]
 Copyright (c) 2013 Terence Parr, Sam Harwell
 Copyright (c) 2017 Ivan Kochurkin (upgrade to Java 8)
 All rights reserved.

*/

// Literals

DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
HEX_LITERAL:        '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?;
OCT_LITERAL:        '0' '_'* [0-7] ([0-7_]* [0-7])? [lL]?;
BINARY_LITERAL:     '0' [bB] [01] ([01_]* [01])? [lL]?;

FLOAT_LITERAL:      (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]?
             |       Digits (ExponentPart [fFdD]? | [fFdD])
             ;

HEX_FLOAT_LITERAL:  '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [fFdD]?;

//CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';
//STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';
STRING_LITERAL:     '"' .*? '"';

// Whitespace and comments
WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);


// Fragment rules

// defines a regex pattern for matching the exponent part of a floating-point literal.
// It matches the e or E character, optionally followed by a + or - sign,
// and one or more digits (as defined by the Digits fragment rule).
fragment ExponentPart
    : [eE] [+-]? Digits
    ;

// defines a regex pattern for matching escape sequences in string literals.
// It can match escape sequences for newline, tab, backspace, form feed, double quote,
// single quote, and backslash characters.
// It can also match octal escape sequences (up to three digits)
// and Unicode escape sequences (using the \u prefix followed by four hexadecimal digits).
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

// defines a regex pattern for matching hexadecimal digits.
// It can match one or more hexadecimal digits,
// optionally separated by underscores (useful for making long hexadecimal literals more readable).
fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

// defines a regex pattern for matching a single hexadecimal digit.
fragment HexDigit
    : [0-9a-fA-F]
    ;

// defines a regex pattern for matching decimal digits.
// It can match one or more digits, optionally separated by underscores
// (useful for making long decimal literals more readable).
fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

// defines a regex pattern for matching a letter or a digit.
// It can match any letter (as defined by the Letter fragment rule) or any decimal digit.
fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

// defines a regex pattern for matching a Unicode letter.
// It can match any ASCII letter, the dollar sign,
// or any Unicode character above 0x7F that is not a surrogate or a control character.
// It can also match Greek and Coptic characters or
// UTF-16 surrogate pairs used for encoding characters above U+FFFF.
fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    | [\u0370-\u03FF] // Greek and Coptic characters
    ;
