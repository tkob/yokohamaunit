lexer grammar YokohamaUnitLexer;

STAR_LBRACKET: '*[' Spaces? -> mode(ABBREVIATION);
HASH1: '#' ;
HASH2: '##' ;
HASH3: '###' ;
HASH4: '####' ;
HASH5: '#####' ;
HASH6: '######' ;
TEST: 'Test:' [ \t]* -> mode(TEST_NAME);
LBRACKET_DEFAULT_MODE: '[' -> type(LBRACKET), mode(TABLE_NAME);
SETUP:    'Setup' -> mode(PHASE_LEADING);
EXERCISE: 'Exercise' -> mode(PHASE_LEADING);
VERIFY:   'Verify'  -> mode(PHASE_LEADING);
TEARDOWN: 'Teardown' -> mode(PHASE_LEADING);
BAR: '|' ;
BAR_EOL: '|' [ \t]* '\r'? '\n' ;
HBAR: '|' [|\-=\:\.\+ \t]* '|' [ \t]* '\r'? '\n' ;
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
IN: 'in' ;
UTABLE: 'Table' ;
CSV: 'CSV' Spaces? '\'' -> mode(IN_FILE_NAME) ;
TSV: 'TSV'  Spaces? '\''-> mode(IN_FILE_NAME) ;
EXCEL: 'Excel' Spaces? '\'' -> mode(IN_BOOK_NAME) ;
WHERE: 'where' ;
EQ: '=' ;
LET: 'Let' ;
BE: 'be' ;
DO: 'Do' ;
A_STUB_OF: 'a' Spaces 'stub' Spaces 'of' Spaces? '`' -> mode(CLASS) ;
SUCH: 'such' ;
METHOD: 'method' Spaces? '`' -> mode(METHOD_PATTERN) ;
RETURNS: 'returns' ;
AN_INSTANCE_OF: 'an' Spaces 'instance' Spaces 'of' Spaces? '`' -> mode(CLASS) ;
AN_INSTANCE: 'an' Spaces 'instance' -> mode(AFTER_AN_INSTANCE) ;
AN_INVOCATION_OF: 'an' Spaces 'invocation' Spaces 'of' Spaces? '`' -> mode(METHOD_PATTERN) ;
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
BACK_TICK: '`' -> mode(IN_BACK_TICK) ;
DOUBLE_QUOTE: '"' -> mode(IN_DOUBLE_QUOTE) ;
SINGLE_QUOTE: '\'' -> mode(IN_SINGLE_QUOTE) ;
WS : Spaces -> skip ;

mode ABBREVIATION;
ShortName: ~[\]\r\n]+ ;
RBRACKET_COLON: ']:' [ \t]* -> mode(LONG_NAME) ;

mode LONG_NAME;
LongName: ~[\r\n]+ ;
NEW_LINE_LONG_NAME: [ \t]* ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode TEST_NAME;
TestName: ~[\r\n]+ ;
NEW_LINE_TEST_NAME: [ \t]* ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode TABLE_NAME;
TableName: ~[\]\r\n]+ ;
RBRACKET_TABLE_NAME: ']' -> type(RBRACKET), mode(DEFAULT_MODE) ;

mode PHASE_LEADING;
COLON: ':' [ \t]* -> skip, mode(PHASE_DESCRIPTION) ;
NEW_LINE_PHASE_LEADING: [ \t]* '\r'? '\n' -> skip, mode(DEFAULT_MODE) ;

mode PHASE_DESCRIPTION;
PhaseDescription: ~[\r\n]+ ;
NEW_LINE_PHASE_DESCRIPTION: ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode AFTER_AN_INSTANCE;
OF: 'of' ;
Identifier_AFTER_AN_INSTANCE : IdentStart IdentPart* -> type(Identifier) ;
BACK_TICK_AFTER_AN_INSTANCE: '`' -> type(BACK_TICK), mode(CLASS) ;
WS_AFTER_AN_INSTANCE: Spaces -> skip ;

mode IN_DOUBLE_QUOTE;
Str: (~["\\\r\n] | UnicodeEscape | EscapeSequence)+ ;
CLOSE_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(DEFAULT_MODE) ;

mode IN_SINGLE_QUOTE;
Char: (~['\\\r\n] | UnicodeEscape | EscapeSequence)+ ;
CLOSE_SINGLE_QUOTE: '\'' -> type(SINGLE_QUOTE), mode(DEFAULT_MODE) ;

mode IN_BACK_TICK;
Expr: ~[`]+ ;
CLOSE_BACK_TICK: '`' -> type(BACK_TICK), mode(DEFAULT_MODE) ;

mode METHOD_PATTERN;
BOOLEAN: 'boolean' ;
BYTE: 'byte' ;
SHORT: 'short' ;
INT: 'int' ;
LONG: 'long' ;
CHAR: 'char' ;
FLOAT: 'float' ;
DOUBLE: 'double' ;
COMMA_METHOD_PATTERN: ',' -> type(COMMA);
THREEDOTS: '...' ;
DOT: '.' ;
LPAREN: '(' ;
RPAREN: ')' ;
LBRACKET: '[' ;
RBRACKET: ']' ;
Identifier_METHOD_PATTERN : IdentStart IdentPart* -> type(Identifier);
WS_METHOD_PATTERN: Spaces -> skip ;
BACK_TICK_METHOD_PATTERN: '`' -> type(BACK_TICK), mode(DEFAULT_MODE) ;

mode CLASS;
DOT_CLASS: '.' -> type(DOT) ;
Identifier_CLASS : IdentStart IdentPart* -> type(Identifier) ;
WS_CLASS: Spaces -> skip ;
BACK_TICK_CLASS: '`' -> type(BACK_TICK), mode(DEFAULT_MODE) ;

mode IN_FILE_NAME;
SingleQuoteName: (~['\r\n]|'\'\'')+ ;
CLOSE_SINGLE_QUOTE_IN_FILE_NAME: '\'' -> type(SINGLE_QUOTE), mode(DEFAULT_MODE) ;

mode IN_BOOK_NAME;
SingleQuoteName3: (~['\r\n]|'\'\'')+ -> type(SingleQuoteName) ;
CLOSE_SINGLE_QUOTE_IN_BOOK_NAME: '\'' -> type(SINGLE_QUOTE), mode(DEFAULT_MODE) ;

fragment
Spaces: [ \t\r\n]+ ;

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

