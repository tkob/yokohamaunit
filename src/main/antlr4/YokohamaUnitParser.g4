parser grammar YokohamaUnitParser;

options { tokenVocab=YokohamaUnitLexer; }

group: definition* ;

definition: test
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

tableRef: ACCORDING TO tableType Quoted ;
tableType: UTABLE | CSV | TSV | EXCEL ;

bindings: WHERE binding (AND binding)* ;
binding: Identifier (EQ | IS) Expr ;

tableDef: TABLE TableName header HBAR? rows ;

header: BAR (Identifier BAR)* Identifier? NEWLINE;

rows: row+ ;
row: BAR (Expr BAR)* Expr? NEWLINE  ;
