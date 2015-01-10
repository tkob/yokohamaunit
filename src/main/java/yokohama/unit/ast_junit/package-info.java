/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= TopBinding* Action* TestStateMent* Action*
 * 
 * TopBinding ::= Expr
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
 * Action ::= QuotedExpr
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