parser grammar YokohamaUnitParser;

options { tokenVocab=YokohamaUnitLexer; }

group: abbreviation* definition* ;

abbreviation: ShortName LongName ;

definition: test
          | fourPhaseTest
          | tableDef
          ;

test: hash? TEST TestName assertion+ ;

hash: HASH1 | HASH2 | HASH3 | HASH4 | HASH5 | HASH6 ;

assertion: ASSERT THAT? propositions condition? STOP ;

propositions: proposition (AND THAT? proposition)* ;

proposition: subject predicate ;

subject: Expr | invokeExpr ;

predicate: isPredicate | isNotPredicate | throwsPredicate ;
isPredicate: IS matcher ;
isNotPredicate: IS NOT matcher ;
throwsPredicate: THROWS matcher ;

matcher: equalTo | instanceOf | instanceSuchThat | nullValue ;
equalTo: argumentExpr ;
instanceOf: AN_INSTANCE_OF classType ;
instanceSuchThat: AN_INSTANCE Identifier OF classType SUCH THAT proposition (AND proposition)*;
nullValue: NULL | NOTHING ;

condition: forAll
         | bindings
         ;

forAll: FOR ALL vars IN tableRef ;
vars: Identifier ((COMMA Identifier)* AND Identifier)? ;
tableRef: UTABLE TableName
        | CSV SingleQuoteName
        | TSV SingleQuoteName
        | EXCEL SingleQuoteName
        ;

bindings: WHERE binding (AND binding)* ;
binding: Identifier (EQ | IS) expr ;

fourPhaseTest: hash? TEST TestName setup? exercise? verify teardown? ;

setup: hash? SETUP PhaseDescription? (letStatement+ execution* | execution+) ;
exercise: hash? EXERCISE PhaseDescription? execution+ ;
verify: hash? VERIFY PhaseDescription? assertion+ ;
teardown: hash? TEARDOWN PhaseDescription? execution+ ;

letStatement: LET letBinding (AND letBinding)* STOP ;
letBinding: Identifier (EQ | BE) expr ;
execution: DO Expr (AND Expr)* STOP ;

tableDef: TableName header HBAR? rows
        | header HBAR? rows TableName ;

header: (BAR Identifier)+ BAR_EOL ;

rows: row+ ;
row: (BAR argumentExpr)+ BAR_EOL  ;

expr: Expr
    | stubExpr
    | invokeExpr
    | integerExpr
    | floatingPointExpr
    | booleanExpr
    | charExpr
    | stringExpr
    ;

stubExpr: A_STUB_OF classType ( SUCH THAT stubBehavior (AND stubBehavior)* )? ;
stubBehavior: METHOD methodPattern RETURNS expr ;

methodPattern: Identifier LPAREN (type COMMA)* (type THREEDOTS?)? RPAREN ;

type : nonArrayType (LBRACKET RBRACKET)* ;
nonArrayType: primitiveType | classType ;
primitiveType: BOOLEAN | BYTE | SHORT | INT | LONG | CHAR | FLOAT | DOUBLE ;
classType: Identifier (DOT Identifier)* ;

invokeExpr: AN_INVOCATION_OF classType DOT methodPattern
            ( ON Expr )?
            ( WITH argumentExpr (COMMA argumentExpr)* )? ;

argumentExpr: Expr
            | integerExpr
            | floatingPointExpr
            | booleanExpr
            | charExpr
            | stringExpr
            ;

integerExpr: MINUS? Integer ;

floatingPointExpr: MINUS? FloatingPoint ;

booleanExpr: TRUE | FALSE ;

charExpr: Char ;

stringExpr: Str | EMPTY_STRING ;