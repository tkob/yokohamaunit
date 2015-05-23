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

assertion: ASSERT THAT? propositions condition? STOP ;

propositions: proposition (AND THAT? proposition)* ;

proposition: subject predicate ;

subject: quotedExpr | invokeExpr ;

predicate: isPredicate | isNotPredicate | throwsPredicate ;
isPredicate: IS matcher ;
isNotPredicate: IS NOT matcher ;
throwsPredicate: THROWS matcher ;

matcher: equalTo | instanceOf | instanceSuchThat | nullValue ;
equalTo: argumentExpr ;
instanceOf: AN_INSTANCE_OF_BACK_TICK classType BACK_TICK;
instanceSuchThat: AN_INSTANCE Identifier OF BACK_TICK classType BACK_TICK SUCH THAT proposition (AND proposition)*;
nullValue: NULL | NOTHING ;

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
binding: singleBinding | choiceBinding ;
singleBinding: Identifier (EQ | IS) expr ;
choiceBinding: Identifier (EQ | IS) ANY_OF expr (COMMA expr)* (OR expr)? ;

fourPhaseTest: TEST Line setup? exercise? verify teardown? ;

setup: SETUP Line? (letStatement+ statement* | statement+) ;
exercise: EXERCISE Line? statement+ ;
verify: VERIFY Line? assertion+ ;
teardown: TEARDOWN Line? statement+ ;

letStatement: LET letBinding (AND letBinding)* STOP ;
letBinding: letSingleBinding | letChoiceBinding ;
letSingleBinding: Identifier (EQ | BE) expr ;
letChoiceBinding: Identifier (EQ | BE) ANY_OF expr (COMMA expr)* (OR expr)? ;

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
    ;

quotedExpr: BACK_TICK Expr BACK_TICK ;

stubExpr: A_STUB_OF_BACK_TICK classType BACK_TICK ( SUCH THAT stubBehavior (AND stubBehavior)* )? ;
stubBehavior: METHOD_BACK_TICK methodPattern BACK_TICK RETURNS expr ;

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
            ;

integerExpr: MINUS? Integer ;

floatingPointExpr: MINUS? FloatingPoint ;

booleanExpr: TRUE | FALSE ;

charExpr: SINGLE_QUOTE Char SINGLE_QUOTE ;

stringExpr: DOUBLE_QUOTE Str DOUBLE_QUOTE | EMPTY_STRING ;

anchorExpr: LBRACKET Anchor RBRACKET ;

codeBlock: heading BACK_TICKS attributes CodeLine* BACK_TICKS ;
attributes: CodeLine ;

heading: HASHES Line ;
