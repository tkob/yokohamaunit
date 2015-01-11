/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= TopBinding* Statement* ActionStatement*
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
 * Statement ::= IsStatement
 *             | IsNotStatement
 *             | ThrowsStatement
 *             | ActionStatement
 * 
 * IsStatement ::= QuotedExpr QuotedExpr
 * IsNotStatement ::= QuotedExpr QuotedExpr
 * ThrowsStatement ::= QuotedExpr QuotedExpr
 * ActionStatement ::= QuotedExpr
 * 
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;