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
BAR: '|' -> mode(IN_TABLE_HEADER) ;
STARLBRACKET: '*[' -> skip, mode(ABBREVIATION);
ASSERT: 'Assert' -> mode(IN_THE_MIDDLE_OF_LINE) ;
THAT: 'that' -> mode(IN_THE_MIDDLE_OF_LINE) ;
STOP: '.' -> mode(IN_THE_MIDDLE_OF_LINE) ;
AND: 'and' -> mode(IN_THE_MIDDLE_OF_LINE) ;
IS: 'is' -> mode(IN_THE_MIDDLE_OF_LINE) ;
NOT: 'not' -> mode(IN_THE_MIDDLE_OF_LINE) ;
THROWS: 'throws' -> mode(IN_THE_MIDDLE_OF_LINE) ;
FOR: 'for' -> mode(IN_THE_MIDDLE_OF_LINE) ;
ALL: 'all' -> mode(IN_THE_MIDDLE_OF_LINE) ;
COMMA: ',' -> mode(IN_THE_MIDDLE_OF_LINE) ;
RULES: 'rules' -> mode(IN_THE_MIDDLE_OF_LINE) ;
IN: 'in' -> mode(IN_THE_MIDDLE_OF_LINE) ;
UTABLE: 'Table' -> mode(AFTER_TABLE) ;
CSV: 'CSV' -> mode(AFTER_CSV) ;
TSV: 'TSV' -> mode(AFTER_CSV) ;
EXCEL: 'Excel' -> mode(AFTER_EXCEL) ;
WHERE: 'where' -> mode(IN_THE_MIDDLE_OF_LINE) ;
EQ: '=' -> mode(IN_THE_MIDDLE_OF_LINE) ;
LET: 'Let' -> mode(IN_THE_MIDDLE_OF_LINE) ;
BE: 'be' -> mode(IN_THE_MIDDLE_OF_LINE) ;
DO: 'Do' -> mode(IN_THE_MIDDLE_OF_LINE) ;
A_STUB_OF: 'a' [ \t\r\n]+ 'stub' [ \t\r\n]+ 'of' -> mode(EXPECT_CLASS) ;
SUCH: 'such' -> mode(IN_THE_MIDDLE_OF_LINE) ;
METHOD: 'method' -> mode(AFTER_METHOD) ;
RETURNS: 'returns' -> mode(IN_THE_MIDDLE_OF_LINE) ;
AN_INSTANCE_OF: 'an' [ \t\r\n]+ 'instance' [ \t\r\n] 'of' -> mode(EXPECT_CLASS) ;
AN_INSTANCE: 'an' [ \t\r\n]+ 'instance' -> mode(AFTER_AN_INSTANCE) ;
AN_INVOCATION_OF: 'an' [ \t\r\n]+ 'invocation' [ \t\r\n] 'of' -> mode(AFTER_METHOD) ;
NULL: 'null' -> mode(IN_THE_MIDDLE_OF_LINE) ;
NOTHING: 'nothing' -> mode(IN_THE_MIDDLE_OF_LINE) ;
TRUE: 'true' -> mode(IN_THE_MIDDLE_OF_LINE) ;
FALSE: 'false' -> mode(IN_THE_MIDDLE_OF_LINE) ;
Identifier:	IdentStart IdentPart* -> mode(IN_THE_MIDDLE_OF_LINE) ;
Integer: IntegerLiteral -> mode(IN_THE_MIDDLE_OF_LINE) ;
FloatingPoint: FloatingPointLiteral -> mode(IN_THE_MIDDLE_OF_LINE) ;
MINUS: '-' -> mode(IN_THE_MIDDLE_OF_LINE) ;
OPENBACKTICK: '`' -> skip, mode(IN_BACKTICK) ;
OPENDOUBLEQUOTE: '"' -> skip, mode(IN_DOUBLEQUOTE) ;
OPENSINGLEQUOTE: '\'' -> skip, mode(IN_SINGLEQUOTE) ;
NEW_LINE : ('\r'? '\n')+ -> skip ;
WS : [ \t]+ -> skip ;

mode IN_THE_MIDDLE_OF_LINE;
ASSERT2: 'Assert' -> type(ASSERT) ;
THAT2: 'that' -> type(THAT) ;
STOP2: '.' -> type(STOP) ;
AND2: 'and' -> type(AND) ;
IS2: 'is' -> type(IS) ;
NOT2: 'not' -> type(NOT) ;
THROWS2: 'throws' -> type(THROWS) ;
FOR2: 'for' -> type(FOR) ;
ALL2: 'all' -> type(ALL) ;
RULES2: 'rules' -> type(RULES) ;
COMMA2: ',' -> type(COMMA);
IN2: 'in' -> type(IN) ;
UTABLE2: 'Table' -> type(UTABLE), mode(AFTER_TABLE) ;
CSV2: 'CSV' -> type(CSV), mode(AFTER_CSV) ;
TSV2: 'TSV' -> type(TSV), mode(AFTER_CSV) ;
EXCEL2: 'Excel' -> type(EXCEL), mode(AFTER_EXCEL) ;
WHERE2: 'where' -> type(WHERE) ;
EQ2: '=' -> type(EQ) ;
LET2: 'Let' -> type(LET) ;
BE2: 'be' -> type(BE) ;
DO2: 'Do' -> type(DO) ;
A_STUB_OF2: 'a' [ \t\r\n]+ 'stub' [ \t\r\n]+ 'of' -> type(A_STUB_OF), mode(EXPECT_CLASS) ;
SUCH2: 'such' -> type(SUCH) ;
METHOD2: 'method' -> type(METHOD), mode(AFTER_METHOD) ;
RETURNS2: 'returns' -> type(RETURNS) ;
AN_INSTANCE_OF2: 'an' [ \t\r\n]+ 'instance' [ \t\r\n] 'of' -> type(AN_INSTANCE_OF), mode(EXPECT_CLASS) ;
AN_INSTANCE2: 'an' [ \t\r\n]+ 'instance' -> type(AN_INSTANCE), mode(AFTER_AN_INSTANCE) ;
AN_INVOCATION_OF2: 'an' [ \t\r\n]+ 'invocation' [ \t\r\n] 'of' -> type(AN_INVOCATION_OF), mode(AFTER_METHOD) ;
NULL2: 'null' -> type(NULL) ;
NOTHING2: 'nothing' -> type(NOTHING) ;
TRUE2: 'true' -> type(TRUE) ;
FALSE2: 'false' -> type(FALSE) ;
Identifier2 : IdentStart IdentPart* -> type(Identifier);
Integer2: IntegerLiteral -> type(Integer);
FloatingPoint2: FloatingPointLiteral -> type(FloatingPoint);
MINUS2: '-' -> type(MINUS) ;
OPENBACKTICK2: '`' -> skip, mode(IN_BACKTICK) ;
OPENDOUBLEQUOTE2: '"' -> skip, mode(IN_DOUBLEQUOTE) ;
OPENSINGLEQUOTE2: '\'' -> skip, mode(IN_SINGLEQUOTE) ;
NEW_LINE2 : ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;
WS2 : [ \t]+ -> skip ;

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
Quoted: ~["]+ ;
CLOSEDOUBLEQUOTE: '"' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode IN_SINGLEQUOTE;
Char: (~['\r\n] | '\\\'')+ ;
CLOSESINGLEQUOTE: '\'' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode IN_BACKTICK;
Expr: ~[`]+ /*-> type(Expr)*/ ;
CLOSEBACKTICK: '`' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode IN_TABLE_HEADER;
IDENTHEADER: IdentStart IdentPart* -> type(Identifier) ;
BARH: '|' -> type(BAR) ;
NEWLINE: '\r'? '\n' -> mode(IN_TABLE_ONSET) ;
SPACETAB: [ \t]+ -> skip ;

mode IN_TABLE_CELL;
ExprCell: ~[|\r\n]+ -> type(Expr) ;
BARCELL: '|' -> type(BAR) ;
NEWLINECELL: '\r'? '\n' -> type(NEWLINE), mode(IN_TABLE_ONSET) ;
SPACETABCELL: [ \t]+ -> skip ;

mode IN_TABLE_ONSET;
HBAR: [|\-=\:\.\+ \t]+ '\r'? '\n' ;
BARONSET: '|' -> type(BAR), mode(IN_TABLE_CELL) ;
TABLECAPTION2: '[' -> skip, mode(TABLE_NAME);
SPACETAB2: [ \t]+ -> skip ;
NEWLINEONSET: '\r'?'\n' -> skip, mode(DEFAULT_MODE) ;

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
CLOSEBACKTICK2: '`' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode EXPECT_CLASS;
OPENBACKTICK4: '`' -> skip, mode(CLASS) ;
SPACETABNEWLINE3: [ \t\r\n]+ -> skip ;

mode CLASS;
DOT2: '.' -> type(DOT) ;
Identifier4 : IdentStart IdentPart* -> type(Identifier) ;
SPACETABNEWLINE4: [ \t\r\n]+ -> skip ;
CLOSEBACKTICK3: '`' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

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
RBRACKET2: ']' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode IN_FILE_NAME;
SingleQuoteName2: (~['\r\n]|'\'\'')* -> type(SingleQuoteName) ;
CLOSESINGLEQUOTE2: '\'' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode IN_BOOK_NAME;
SingleQuoteName3: (~['\r\n]|'\'\'')* -> type(SingleQuoteName) ;
CLOSESINGLEQUOTE3: '\'' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

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