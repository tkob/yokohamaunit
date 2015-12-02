parser grammar YokohamaUnitParser;

options { tokenVocab=YokohamaUnitLexer; }

group: abbreviation* definition* ;

abbreviation: STAR_LBRACKET ShortName RBRACKET_COLON Line ;

definition: test
          | fourPhaseTest
          | tableDef
          | codeBlock
          | heading
          ;

test: TEST Line assertion+ ;

assertion: ASSERT THAT? clauses condition? STOP ;

clauses: clause (AND THAT? clause)* ;
clause: proposition (OR proposition)* ;
proposition: subject predicate ;

subject: quotedExpr | invokeExpr ;

predicate: isPredicate
         | isNotPredicate
         | throwsPredicate
         | matchesPredicate
         | doesNotMatchPredicate
         ;
isPredicate: IS matcher ;
isNotPredicate: IS NOT matcher ;
throwsPredicate: THROWS matcher ;
matchesPredicate: MATCHES pattern ;
doesNotMatchPredicate: DOES NOT MATCH pattern ;

matcher: equalTo | instanceOf | instanceSuchThat | nullValue ;
equalTo: argumentExpr ;
instanceOf: AN_INSTANCE_OF_BACK_TICK classType BACK_TICK;
instanceSuchThat: AN_INSTANCE Identifier OF BACK_TICK classType BACK_TICK SUCH THAT proposition (AND proposition)*;
nullValue: NULL | NOTHING ;

pattern: regexp ;
regexp: (RE_TICK | REGEX_TICK | REGEXP_TICK) Regexp BACK_TICK ;

condition: forAll
         | bindings
         ;

forAll: FOR ALL vars IN tableRef ;
vars: Identifier ((COMMA Identifier)* AND Identifier)? ;
tableRef: UTABLE LBRACKET Anchor RBRACKET
        | CSV_SINGLE_QUOTE FileName SINGLE_QUOTE
        | TSV_SINGLE_QUOTE FileName SINGLE_QUOTE
        | EXCEL_SINGLE_QUOTE BookName SINGLE_QUOTE
        ;

bindings: WHERE binding (AND binding)* ;
binding: singleBinding | choiceBinding | tableBinding ;
singleBinding: Identifier (EQ | IS) expr ;
choiceBinding: Identifier (EQ | IS) ANY_OF expr (COMMA expr)* (OR expr)? ;
tableBinding: Identifier (COMMA Identifier)* (EQ | IS) ANY_DEFINED_BY tableRef ;

fourPhaseTest: TEST Line setup? exercise? verify teardown? ;

setup: SETUP Line? (letStatement+ statement* | statement+) ;
exercise: EXERCISE Line? statement+ ;
verify: VERIFY Line? assertion+ ;
teardown: TEARDOWN Line? statement+ ;

letStatement: LET letBinding (AND letBinding)* STOP ;
letBinding: letSingleBinding | letChoiceBinding | letTableBinding ;
letSingleBinding: Identifier (EQ | BE) expr ;
letChoiceBinding: Identifier (EQ | BE) ANY_OF expr (COMMA expr)* (OR expr)? ;
letTableBinding: Identifier (COMMA Identifier)* (EQ | BE) ANY_DEFINED_BY tableRef ;

statement: execution | invoke ;
execution: DO quotedExpr (AND quotedExpr)* STOP ;
invoke: INVOKE_TICK classType (DOT | HASH) methodPattern BACK_TICK
            ( ON quotedExpr)?
            ( WITH argumentExpr (COMMA argumentExpr)* )?
            STOP;

tableDef: LBRACKET Anchor RBRACKET header HBAR? rows
        | header HBAR? rows LBRACKET Anchor RBRACKET ;

header: (BAR Identifier)+ BAR_EOL ;

rows: row+ ;
row: (BAR argumentExpr)+ BAR_EOL  ;

expr: quotedExpr
    | stubExpr
    | invokeExpr
    | integerExpr
    | floatingPointExpr
    | booleanExpr
    | charExpr
    | stringExpr
    | anchorExpr
    | asExpr
    | resourceExpr
    | tempFileExpr
    ;

quotedExpr: BACK_TICK Expr BACK_TICK ;

stubExpr: A_STUB_OF_BACK_TICK classType BACK_TICK ( SUCH THAT stubBehavior (AND stubBehavior)* )? ;
stubBehavior: stubReturns | stubThrows ;
stubReturns: METHOD_BACK_TICK methodPattern BACK_TICK RETURNS expr ;
stubThrows: METHOD_BACK_TICK methodPattern BACK_TICK THROWS expr ;

methodPattern: Identifier LPAREN (type COMMA)* (type THREEDOTS?)? RPAREN ;

type : nonArrayType (LBRACKET RBRACKET)* ;
nonArrayType: primitiveType | classType ;
primitiveType: BOOLEAN | BYTE | SHORT | INT | LONG | CHAR | FLOAT | DOUBLE ;
classType: Identifier (DOT Identifier)* ;

invokeExpr: AN_INVOCATION_OF_BACK_TICK classType (DOT | HASH) methodPattern BACK_TICK
            ( ON quotedExpr)?
            ( WITH argumentExpr (COMMA argumentExpr)* )? ;

argumentExpr: quotedExpr
            | integerExpr
            | floatingPointExpr
            | booleanExpr
            | charExpr
            | stringExpr
            | anchorExpr
            | resourceExpr
            ;

integerExpr: MINUS? Integer ;

floatingPointExpr: MINUS? FloatingPoint ;

booleanExpr: TRUE | FALSE ;

charExpr: SINGLE_QUOTE Char SINGLE_QUOTE ;

stringExpr: DOUBLE_QUOTE Str DOUBLE_QUOTE | EMPTY_STRING ;

anchorExpr: LBRACKET Anchor RBRACKET ;

asExpr: sourceExpr AS_BACK_TICK classType BACK_TICK ;
sourceExpr: stringExpr | anchorExpr ;

resourceExpr: RESOURCE DOUBLE_QUOTE Str DOUBLE_QUOTE (AS_BACK_TICK classType BACK_TICK)? ;

tempFileExpr: A_TEMPORARY_FILE | A_TEMP_FILE ;

codeBlock: heading BACK_TICKS attributes CodeLine* BACK_TICKS ;
attributes: CodeLine ;

heading: HASHES Line ;
