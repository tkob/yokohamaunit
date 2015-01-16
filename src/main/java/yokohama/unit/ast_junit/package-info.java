/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= Statement* ActionStatement*
 * 
 * Expr ::= QuotedExpr | StubExpr | VarExpr | MatcherExpr
 * 
 * StubExpr ::= QuotedExpr StubBehavior
 * StubBehavior ::= MethodPattern Expr
 * MethodPattern ::= Type*
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * MatcherExpr ::= InstanceOfMatcherExpr
 * 
 * Statement ::= IsStatement
 *             | IsNotStatement
 *             | ThrowsStatement
 *             | ActionStatement
 *             | TopBindStatement
 *             | VarDeclStatement
 *             | BindThrownStatement
 * 
 * IsStatement ::= QuotedExpr QuotedExpr
 * IsNotStatement ::= QuotedExpr QuotedExpr
 * ThrowsStatement ::= QuotedExpr QuotedExpr
 * ActionStatement ::= QuotedExpr
 * TopBindStatement ::= Expr
 * VarDeclStatement ::= Expr
 * BindThrownStatement ::= Expr
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;