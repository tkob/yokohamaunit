/**
 * Abstract Syntax Tree.
 * 
 * <pre>
 * Group ::= Definition*
 * 
 * Definition ::= Test | Table
 * 
 * Test ::= Assertion*
 * 
 * Assertion ::= Proposition* Fixture
 * 
 * Proposition ::= Expr Copula Expr
 * 
 * Copula ::= IS | IS_NOT | THROWS
 * 
 * Fixture ::= | TableRef | Bindings
 * 
 * TableRef ::= TableType
 * 
 * TableType :: INLINE | CSV | TSV | EXCEL
 * 
 * Bindings ::= Binding*
 * 
 * Table ::= Row*
 * 
 * Row ::= Expr*
 * </pre>
 * 
 */
package yokohama.unit.ast;