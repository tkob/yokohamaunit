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
 * Expr ::= QuotedExpr | StubExpr | MatcherExpr | NewExpr | StrLitExpr
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
 *             | VarDeclStatement
 *             | BindThrownStatement
 *             | ReturnIsStatement
 *             | ReturnIsNotStatement
 *             | InvokeVoidStatement
 *             | TryStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * ActionStatement ::= QuotedExpr
 * VarDeclStatement ::= Expr
 * BindThrownStatement ::= Expr
 * ReturnIsStatement ::= Var Var
 * ReturnIsNotStatement ::= Var Var
 * InvokeVoidStatement ::= Var Var*
 * TryStatement ::= Statement* CatchClause* Statement*
 * 
 * CatchClause ::= ClassType Var Statement*
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;
