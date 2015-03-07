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
 * Expr ::= VarExpr
 *        | QuotedExpr
 *        | StubExpr
 *        | MatcherExpr
 *        | NewExpr
 *        | StrLitExpr
 *        | NullExpr
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
 *             | VarInitStatement
 *             | BindThrownStatement
 *             | ReturnIsStatement
 *             | ReturnIsNotStatement
 *             | InvokeVoidStatement
 *             | TryStatement
 *             | VarDeclStatement
 *             | VarAssignStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * ActionStatement ::= QuotedExpr
 * VarInitStatement ::= Expr
 * BindThrownStatement ::= Expr
 * ReturnIsStatement ::= Var Var
 * ReturnIsNotStatement ::= Var Var
 * InvokeVoidStatement ::= Var Var*
 * TryStatement ::= Statement* CatchClause* Statement*
 * VarDeclStatement ::= ClassType
 * VarAssignStatement ::= Expr
 * 
 * CatchClause ::= ClassType Var Statement*
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;
