lexer grammar YokohamaUnitLexer;

HASH1: '#' ;
HASH2: '##' ;
HASH3: '###' ;
HASH4: '####' ;
HASH5: '#####' ;
HASH6: '######' ;
TEST: 'Test:' -> mode(TEST_LEADING);
TABLECAPTION: '[' -> skip, mode(TABLE_NAME);
SETUP:    'Setup' -> mode(PHASE_LEADING);
EXERCISE: 'Exercise' -> mode(PHASE_LEADING);
VERIFY:   'Verify'  -> mode(PHASE_LEADING);
TEARDOWN: 'Teardown' -> mode(PHASE_LEADING);
BAR_EOL: '|' [ \t]* '\r'? '\n' ;
BAR: '|' ;
HBAR: '|' [|\-=\:\.\+ \t]* '|' [ \t]* '\r'? '\n' ;
STARLBRACKET: '*[' -> skip, mode(ABBREVIATION);
ASSERT: 'Assert' ;
THAT: 'that' ;
STOP: '.' ;
AND: 'and' ;
IS: 'is' ;
NOT: 'not' ;
THROWS: 'throws' ;
FOR: 'for' ;
ALL: 'all' ;
COMMA: ',' ;
RULES: 'rules' ;
IN: 'in' ;
UTABLE: 'Table' -> mode(AFTER_TABLE) ;
CSV: 'CSV' -> mode(AFTER_CSV) ;
TSV: 'TSV' -> mode(AFTER_CSV) ;
EXCEL: 'Excel' -> mode(AFTER_EXCEL) ;
WHERE: 'where' ;
EQ: '=' ;
LET: 'Let' ;
BE: 'be' ;
DO: 'Do' ;
A_STUB_OF: 'a' [ \t\r\n]+ 'stub' [ \t\r\n]+ 'of' -> mode(EXPECT_CLASS) ;
SUCH: 'such' ;
METHOD: 'method' -> mode(AFTER_METHOD) ;
RETURNS: 'returns' ;
AN_INSTANCE_OF: 'an' [ \t\r\n]+ 'instance' [ \t\r\n]+ 'of' -> mode(EXPECT_CLASS) ;
AN_INSTANCE: 'an' [ \t\r\n]+ 'instance' -> mode(AFTER_AN_INSTANCE) ;
AN_INVOCATION_OF: 'an' [ \t\r\n]+ 'invocation' [ \t\r\n]+ 'of' -> mode(AFTER_METHOD) ;
ON: 'on' ;
WITH: 'with' ;
NULL: 'null' ;
NOTHING: 'nothing' ;
TRUE: 'true' ;
FALSE: 'false' ;
Identifier:	IdentStart IdentPart* ;
Integer: IntegerLiteral ;
FloatingPoint: FloatingPointLiteral ;
MINUS: '-' ;
EMPTY_STRING: '""' ;
OPENBACKTICK: '`' -> skip, mode(IN_BACKTICK) ;
OPENDOUBLEQUOTE: '"' -> skip, mode(IN_DOUBLEQUOTE) ;
OPENSINGLEQUOTE: '\'' -> skip, mode(IN_SINGLEQUOTE) ;
NEW_LINE : ('\r'? '\n')+ -> skip ;
WS : [ \t]+ -> skip ;

mode TEST_LEADING;
WS3: [ \t]+ -> skip, mode(TEST_NAME);

mode TEST_NAME;
TestName: ~[\r\n]+ ;
NEW_LINE_TESTNAME: [ \t]* ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode TABLE_NAME;
TableName: ~[\]\r\n]+ ;
EXIT_TABLENAME: ']' [ \t]* ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode PHASE_LEADING;
COLON: ':' [ \t]* -> skip, mode(PHASE_DESCRIPTION) ;
WS5: [ \t]* '\r'? '\n' -> skip, mode(DEFAULT_MODE) ;

mode PHASE_DESCRIPTION;
PhaseDescription: ~[\r\n]+ ;
NEW_LINE_PHASE: ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode IN_DOUBLEQUOTE;
Str: (~["\\\r\n] | UnicodeEscape | EscapeSequence)+ ;
CLOSEDOUBLEQUOTE: '"' -> skip, mode(DEFAULT_MODE) ;

mode IN_SINGLEQUOTE;
Char: (~['\\\r\n] | UnicodeEscape | EscapeSequence)+ ;
CLOSESINGLEQUOTE: '\'' -> skip, mode(DEFAULT_MODE) ;

mode IN_BACKTICK;
Expr: ~[`]+ /*-> type(Expr)*/ ;
CLOSEBACKTICK: '`' -> skip, mode(DEFAULT_MODE) ;

mode AFTER_METHOD;
OPENBACKTICK3: '`' -> skip, mode(METHOD_PATTERN) ;
SPACETABNEWLINE: [ \t\r\n]+ -> skip ;

mode METHOD_PATTERN;
BOOLEAN: 'boolean' ;
BYTE: 'byte' ;
SHORT: 'short' ;
INT: 'int' ;
LONG: 'long' ;
CHAR: 'char' ;
FLOAT: 'float' ;
DOUBLE: 'double' ;
COMMA3: ',' -> type(COMMA);
THREEDOTS: '...' ;
DOT: '.' ;
LPAREN: '(' ;
RPAREN: ')' ;
LBRACKET: '[' ;
RBRACKET: ']' ;
Identifier3 : IdentStart IdentPart* -> type(Identifier);
SPACETABNEWLINE2: [ \t\r\n]+ -> skip ;
CLOSEBACKTICK2: '`' -> skip, mode(DEFAULT_MODE) ;

mode EXPECT_CLASS;
OPENBACKTICK4: '`' -> skip, mode(CLASS) ;
SPACETABNEWLINE3: [ \t\r\n]+ -> skip ;

mode CLASS;
DOT2: '.' -> type(DOT) ;
Identifier4 : IdentStart IdentPart* -> type(Identifier) ;
SPACETABNEWLINE4: [ \t\r\n]+ -> skip ;
CLOSEBACKTICK3: '`' -> skip, mode(DEFAULT_MODE) ;

mode AFTER_AN_INSTANCE;
OF: 'of' ;
Identifier5 : IdentStart IdentPart* -> type(Identifier) ;
OPENBACKTICK5: '`' -> skip, mode(CLASS) ;
SPACETABNEWLINE5: [ \t\r\n]+ -> skip ;

mode AFTER_TABLE;
LBRACKET2: '[' -> skip, mode(IN_TABLE_NAME) ;
SPACETABNEWLINE6: [ \t\r\n]+ -> skip ;

mode AFTER_CSV;
OPENSINGLEQUOTE3: '\'' -> skip, mode(IN_FILE_NAME) ;
SPACETABNEWLINE7: [ \t\r\n]+ -> skip ;

mode AFTER_EXCEL;
OPENSINGLEQUOTE4: '\'' -> skip, mode(IN_BOOK_NAME) ;
SPACETABNEWLINE8: [ \t\r\n]+ -> skip ;

mode IN_TABLE_NAME;
SingleQuoteName: ~[\]\r\n]* ;
RBRACKET2: ']' -> skip, mode(DEFAULT_MODE) ;

mode IN_FILE_NAME;
SingleQuoteName2: (~['\r\n]|'\'\'')* -> type(SingleQuoteName) ;
CLOSESINGLEQUOTE2: '\'' -> skip, mode(DEFAULT_MODE) ;

mode IN_BOOK_NAME;
SingleQuoteName3: (~['\r\n]|'\'\'')* -> type(SingleQuoteName) ;
CLOSESINGLEQUOTE3: '\'' -> skip, mode(DEFAULT_MODE) ;

mode ABBREVIATION;
ShortName: ~[\]\r\n]* ;
RBRACKETCOLON: ']:' [ \t\r\n]* -> skip, mode(LONG_NAME) ;

mode LONG_NAME;
LongName: ~[\r\n]* ;
EXIT_LONG_NAME: ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

fragment
IdentStart: ~[\uD800-\uDBFF]
            {Character.isJavaIdentifierStart(_input.LA(-1))}?
          | [\uD800-\uDBFF] [\uDC00-\uDFFF]
            {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
          ;

fragment
IdentPart: ~[\uD800-\uDBFF]
           {Character.isJavaIdentifierPart(_input.LA(-1))}?
         | [\uD800-\uDBFF] [\uDC00-\uDFFF]
		   {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
         ;

fragment
IntegerLiteral: DecimalIntegerLiteral 
              | HexIntegerLiteral 
              | OctalIntegerLiteral 
              | BinaryIntegerLiteral
              ;

fragment
DecimalIntegerLiteral: DecimalNumeral IntegerTypeSuffix? ;

fragment
HexIntegerLiteral: HexNumeral IntegerTypeSuffix? ;

fragment
OctalIntegerLiteral: OctalNumeral IntegerTypeSuffix? ;

fragment
BinaryIntegerLiteral: BinaryNumeral IntegerTypeSuffix? ;

fragment
IntegerTypeSuffix: [lL] ;

fragment
DecimalNumeral: '0' | [1-9] ([_0-9]* [0-9])? ;

fragment
HexNumeral: '0' [xX] HexDigits ;
 
fragment
HexDigits: [0-9a-fA-F] ([_0-9a-fA-F]* [0-9a-fA-F])? ;

fragment
OctalNumeral: '0' [_0-7]* [0-7] ;

fragment
BinaryNumeral: '0' [bB] [01] ([_01]* [01])? ;

fragment
FloatingPointLiteral: DecimalFloatingPointLiteral
                    | HexadecimalFloatingPointLiteral
                    ;

fragment
DecimalFloatingPointLiteral: Digits '.' Digits ExponentPart? FloatTypeSuffix?
                           | Digits '.' ExponentPart FloatTypeSuffix?
                           | Digits '.' ExponentPart? FloatTypeSuffix
                             /* the above rules differ from the Java spec:
                                fp literals which end with dot are not allowd */
                           | '.' Digits ExponentPart? FloatTypeSuffix?
                           | Digits ExponentPart FloatTypeSuffix?
                           | Digits ExponentPart? FloatTypeSuffix 
                           ;
fragment
Digits: [0-9] ([_0-9]* [0-9])? ;

fragment
ExponentPart: [eE] SignedInteger ;
 
fragment
SignedInteger: ('+' | '-')? Digits ;

fragment
FloatTypeSuffix: [fFdD] ;

fragment
HexadecimalFloatingPointLiteral: HexSignificand BinaryExponent FloatTypeSuffix? ;

fragment
HexSignificand: HexNumeral '.'?
              | '0' [xX] HexDigits? . HexDigits
              ;
 
fragment
BinaryExponent: [pP] SignedInteger ;

fragment
UnicodeEscape: '\\' 'u'+ [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] ;

fragment
EscapeSequence: '\\b'  // (backspace BS, Unicode \u0008) 
              | '\\t'  // (horizontal tab HT, Unicode \u0009) 
              | '\\n'  // (linefeed LF, Unicode \u000a) 
              | '\\f'  // (form feed FF, Unicode \u000c) 
              | '\\r'  // (carriage return CR, Unicode \u000d)
              | '\\"'  // (double quote ", Unicode \u0022) 
              | '\\\'' // (single quote ', Unicode \u0027) 
              | '\\\\' // (backslash \, Unicode \u005c) 
              | OctalEscape // (octal value, Unicode \u0000 to \u00ff) 
              ;
fragment
OctalEscape: '\\' [0-7]
           | '\\' [0-7] [0-7]
           | '\\' [0-3] [0-7] [0-7]
           ;

