parser grammar YokohamaUnitParser;

options { tokenVocab=YokohamaUnitLexer; }

group: definition* ;

definition: test
          | fourPhaseTest
          | tableDef
          ;

test: hash? TEST TestName assertion+ ;

hash: HASH1 | HASH2 | HASH3 | HASH4 | HASH5 | HASH6 ;

assertion: ASSERT THAT? propositions condition? STOP ;

propositions: proposition (AND THAT? proposition)* ;

proposition: Expr copula Expr ;

copula: IS
      | IS NOT
      | THROWS
      ;

condition: tableRef
         | bindings
         ;

tableRef: FOR ALL RULES IN tableType Quoted ;
tableType: UTABLE | CSV | TSV | EXCEL ;

bindings: WHERE binding (AND binding)* ;
binding: Identifier (EQ | IS) expr ;

fourPhaseTest: hash? TEST TestName setup? exercise? verify teardown? ;

setup: hash? SETUP PhaseDescription? (letBindings execution* | execution+) ;
exercise: hash? EXERCISE PhaseDescription? execution+ ;
verify: hash? VERIFY PhaseDescription? assertion+ ;
teardown: hash? TEARDOWN PhaseDescription? execution+ ;

letBindings: LET letBinding (AND letBinding)* STOP ;
letBinding: Identifier (EQ | BE) expr ;
execution: DO Expr (AND Expr)* STOP ;

tableDef: TABLE TableName header HBAR? rows ;

header: BAR (Identifier BAR)* Identifier? NEWLINE;

rows: row+ ;
row: BAR (Expr BAR)* Expr? NEWLINE  ;

expr: Expr | stubExpr ;

stubExpr: A STUB OF Expr ( SUCHTHAT stubBehavior (AND stubBehavior)* )? ;
stubBehavior: methodPattern RETURNS expr ;

methodPattern: Identifier LPAREN (type COMMA)* (type THREEDOTS?)? RPAREN ;

type : nonArrayType (LBRACKET RBRACKET)* ;
nonArrayType: primitiveType | classType ;
primitiveType: BOOLEAN | BYTE | SHORT | INT | LONG | CHAR | FLOAT | DOUBLE ;
classType: Identifier (DOT Identifier)* ;

