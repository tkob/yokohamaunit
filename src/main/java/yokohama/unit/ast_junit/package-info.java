/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= TopBinding* Action* Statement* Action*
 * 
 * TopBinding ::= Expr
 * 
 * Expr ::= QuotedExpr | StubExpr | VarExpr
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
 * Statement ::= IsStatement
 *             | IsNotStatement
 *             | ThrowsStatement
 * 
 * IsStatement ::= QuotedExpr QuotedExpr
 * IsNotStatement ::= QuotedExpr QuotedExpr
 * ThrowsStatement ::= QuotedExpr QuotedExpr
 * 
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;