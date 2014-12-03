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
ACCORDING: 'according' -> mode(IN_THE_MIDDLE_OF_LINE) ;
TO: 'to' -> mode(IN_THE_MIDDLE_OF_LINE) ;
UTABLE: 'Table' -> mode(IN_THE_MIDDLE_OF_LINE) ;
CSV: 'CSV' -> mode(IN_THE_MIDDLE_OF_LINE) ;
TSV: 'TSV' -> mode(IN_THE_MIDDLE_OF_LINE) ;
EXCEL: 'Excel' -> mode(IN_THE_MIDDLE_OF_LINE) ;
WHERE: 'where' -> mode(IN_THE_MIDDLE_OF_LINE) ;
EQ: '=' -> mode(IN_THE_MIDDLE_OF_LINE) ;
Identifier:	IdentStart IdentPart* ;
LET: 'Let' -> mode(IN_THE_MIDDLE_OF_LINE) ;
BE: 'be' -> mode(IN_THE_MIDDLE_OF_LINE) ;
DO: 'Do' -> mode(IN_THE_MIDDLE_OF_LINE) ;
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
ACCORDING2: 'according' -> type(ACCORDING) ;
TO2: 'to' -> type(TO) ;
UTABLE2: 'Table' -> type(UTABLE) ;
CSV2: 'CSV' -> type(CSV) ;
TSV2: 'TSV' -> type(TSV) ;
EXCEL2: 'Excel' -> type(EXCEL) ;
WHERE2: 'where' -> type(WHERE) ;
EQ2: '=' -> type(EQ) ;
Identifier2 : IdentStart IdentPart* -> type(Identifier);
BE2: 'be' -> type(BE) ;
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
