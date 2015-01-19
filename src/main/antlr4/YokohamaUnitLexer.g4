lexer grammar YokohamaUnitLexer;

HASH1: '#' ;
HASH2: '##' ;
HASH3: '###' ;
HASH4: '####' ;
HASH5: '#####' ;
HASH6: '######' ;
TEST: 'Test:' -> mode(TEST_LEADING);
TABLE: 'Table:' -> mode(TABLE_LEADING);
SETUP:    'Setup' -> mode(PHASE_LEADING);
EXERCISE: 'Exercise' -> mode(PHASE_LEADING);
VERIFY:   'Verify'  -> mode(PHASE_LEADING);
TEARDOWN: 'Teardown' -> mode(PHASE_LEADING);
BAR: '|' -> mode(IN_TABLE_HEADER) ;
ASSERT: 'Assert' -> mode(IN_THE_MIDDLE_OF_LINE) ;
THAT: 'that' -> mode(IN_THE_MIDDLE_OF_LINE) ;
STOP: '.' -> mode(IN_THE_MIDDLE_OF_LINE) ;
AND: 'and' -> mode(IN_THE_MIDDLE_OF_LINE) ;
IS: 'is' -> mode(IN_THE_MIDDLE_OF_LINE) ;
NOT: 'not' -> mode(IN_THE_MIDDLE_OF_LINE) ;
THROWS: 'throws' -> mode(IN_THE_MIDDLE_OF_LINE) ;
FOR: 'for' -> mode(IN_THE_MIDDLE_OF_LINE) ;
ALL: 'all' -> mode(IN_THE_MIDDLE_OF_LINE) ;
RULES: 'rules' -> mode(IN_THE_MIDDLE_OF_LINE) ;
IN: 'in' -> mode(IN_THE_MIDDLE_OF_LINE) ;
UTABLE: 'Table' -> mode(IN_THE_MIDDLE_OF_LINE) ;
CSV: 'CSV' -> mode(IN_THE_MIDDLE_OF_LINE) ;
TSV: 'TSV' -> mode(IN_THE_MIDDLE_OF_LINE) ;
EXCEL: 'Excel' -> mode(IN_THE_MIDDLE_OF_LINE) ;
WHERE: 'where' -> mode(IN_THE_MIDDLE_OF_LINE) ;
EQ: '=' -> mode(IN_THE_MIDDLE_OF_LINE) ;
LET: 'Let' -> mode(IN_THE_MIDDLE_OF_LINE) ;
BE: 'be' -> mode(IN_THE_MIDDLE_OF_LINE) ;
DO: 'Do' -> mode(IN_THE_MIDDLE_OF_LINE) ;
A_STUB_OF: 'a' [ \t\r\n]+ 'stub' [ \t\r\n]+ 'of' -> mode(IN_THE_MIDDLE_OF_LINE) ;
SUCH: 'such' -> mode(IN_THE_MIDDLE_OF_LINE) ;
METHOD: 'method' -> mode(AFTER_METHOD) ;
RETURNS: 'returns' -> mode(IN_THE_MIDDLE_OF_LINE) ;
AN_INSTANCE_OF: 'an' [ \t\r\n]+ 'instance' [ \t\r\n] 'of' -> mode(AFTER_AN_INSTANCE_OF) ;
NULL: 'null' -> mode(IN_THE_MIDDLE_OF_LINE) ;
NOTHING: 'nothing' -> mode(IN_THE_MIDDLE_OF_LINE) ;
Identifier:	IdentStart IdentPart* ;
OPENBACKTICK: '`' -> skip, mode(IN_BACKTICK) ;
OPENDOUBLEQUOTE: '"' -> skip, mode(IN_DOUBLEQUOTE) ;
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
IN2: 'in' -> type(IN) ;
UTABLE2: 'Table' -> type(UTABLE) ;
CSV2: 'CSV' -> type(CSV) ;
TSV2: 'TSV' -> type(TSV) ;
EXCEL2: 'Excel' -> type(EXCEL) ;
WHERE2: 'where' -> type(WHERE) ;
EQ2: '=' -> type(EQ) ;
LET2: 'Let' -> type(LET) ;
BE2: 'be' -> type(BE) ;
DO2: 'Do' -> type(DO) ;
A_STUB_OF2: 'a' [ \t\r\n]+ 'stub' [ \t\r\n]+ 'of' -> type(A_STUB_OF) ;
SUCH2: 'such' -> type(SUCH) ;
METHOD2: 'method' -> type(METHOD), mode(AFTER_METHOD) ;
RETURNS2: 'returns' -> type(RETURNS) ;
AN_INSTANCE_OF2: 'an' [ \t\r\n]+ 'instance' [ \t\r\n] 'of' -> type(AN_INSTANCE_OF), mode(AFTER_AN_INSTANCE_OF) ;
NULL2: 'null' -> type(NULL) ;
NOTHING2: 'nothing' -> type(NOTHING) ;
Identifier2 : IdentStart IdentPart* -> type(Identifier);
OPENBACKTICK2: '`' -> skip, mode(IN_BACKTICK) ;
OPENDOUBLEQUOTE2: '"' -> skip, mode(IN_DOUBLEQUOTE) ;
NEW_LINE2 : ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;
WS2 : [ \t]+ -> skip ;

mode TEST_LEADING;
WS3: [ \t]+ -> skip, mode(TEST_NAME);

mode TEST_NAME;
TestName: ~[\r\n]+ ;
NEW_LINE_TESTNAME: [ \t]* ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode TABLE_LEADING;
WS4: [ \t]+ -> skip, mode(TABLE_NAME);

mode TABLE_NAME;
TableName: ~[\r\n]+ ;
NEW_LINE_TABLENAME: [ \t]* ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode PHASE_LEADING;
COLON: ':' [ \t]* -> skip, mode(PHASE_DESCRIPTION) ;
WS5: [ \t]* '\r'? '\n' -> skip, mode(DEFAULT_MODE) ;

mode PHASE_DESCRIPTION;
PhaseDescription: ~[\r\n]+ ;
NEW_LINE_PHASE: ('\r'? '\n')+ -> skip, mode(DEFAULT_MODE) ;

mode IN_DOUBLEQUOTE;
Quoted: ~["]+ ;
CLOSEDOUBLEQUOTE: '"' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

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
BARONSET: '|' -> type(BAR), mode(IN_TABLE_CELL) ;
HBAR: '-'+ '\r'? '\n' ;
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
COMMA: ',' ;
THREEDOTS: '...' ;
DOT: '.' ;
LPAREN: '(' ;
RPAREN: ')' ;
LBRACKET: '[' ;
RBRACKET: ']' ;
Identifier3 : IdentStart IdentPart* -> type(Identifier);
SPACETABNEWLINE2: [ \t\r\n]+ -> skip ;
CLOSEBACKTICK2: '`' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

mode AFTER_AN_INSTANCE_OF;
OPENBACKTICK4: '`' -> skip, mode(CLASS) ;
SPACETABNEWLINE3: [ \t\r\n]+ -> skip ;

mode CLASS;
DOT2: '.' -> type(DOT) ;
Identifier4 : IdentStart IdentPart* -> type(Identifier) ;
SPACETABNEWLINE4: [ \t\r\n]+ -> skip ;
CLOSEBACKTICK3: '`' -> skip, mode(IN_THE_MIDDLE_OF_LINE) ;

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
