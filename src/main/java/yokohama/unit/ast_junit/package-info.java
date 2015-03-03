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
 * Expr ::= QuotedExpr | StubExpr | MatcherExpr | NewExpr
 * 
 * StubExpr ::= QuotedExpr StubBehavior
 * StubBehavior ::= MethodPattern Expr
 * MethodPattern ::= Type*
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * MatcherExpr ::= InstanceOfMatcherExpr
 *               | NullValueMatcherExpr
 *               | ConjunctionMatcherExpr 
 *               | EqualToMatcherExpr
 *               | SuchThatMatcherExpr 
 * 
 * Statement ::= IsStatement
 *             | IsNotStatement
 *             | ThrowsStatement
 *             | ActionStatement
 *             | TopBindStatement
 *             | VarDeclStatement
 *             | BindThrownStatement
 *             | ReturnIsStatement
 *             | ReturnIsNotStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * ActionStatement ::= QuotedExpr
 * TopBindStatement ::= Expr
 * VarDeclStatement ::= Expr
 * BindThrownStatement ::= Expr
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;