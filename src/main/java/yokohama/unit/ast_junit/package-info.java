/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= Statement* Statement* Statement*
 * 
 * Expr ::= VarExpr
 *        | QuotedExpr
 *        | StubExpr
 *        | MatcherExpr
 *        | NewExpr
 *        | StrLitExpr
 *        | NullExpr
 *        | InvokeExpr
 *        | ThisExpr
 *        | InvokeStaticExpr
 *        | IntLitExpr
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
 * VarInitStatement ::= ClassType Expr
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
