/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= Binding* ActionStatement* TestStateMent* ActionStatement*
 * 
 * Binding ::= Expr
 * 
 * Expr ::= QuotedExpr | StubExpr
 * 
 * StubExpr ::= QuotedExpr StubBehavior
 * StubBehavior ::= MethodPattern Expr
 * MethodPattern ::= Type*
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * ActionStatement ::= QuotedExpr
 * 
 * TestStatement ::= IsStatement
 *                 | IsNotStatement
 *                 | ThrowsStatement
 * 
 * IsStatement ::= QuotedExpr QuotedExpr
 * IsNotStatement ::= QuotedExpr QuotedExpr
 * ThrowsStatement ::= QuotedExpr QuotedExpr
 * 
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;