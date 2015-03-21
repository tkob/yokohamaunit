/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= Statement*
 * 
 * Expr ::= VarExpr
 *        | MatcherExpr
 *        | NewExpr
 *        | StrLitExpr
 *        | NullExpr
 *        | InvokeExpr
 *        | ThisExpr
 *        | InvokeStaticExpr
 *        | IntLitExpr
 *        | ClassLitExpr
 * 
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
 *             | VarInitStatement
 *             | ReturnIsStatement
 *             | ReturnIsNotStatement
 *             | InvokeVoidStatement
 *             | TryStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * VarInitStatement ::= ClassType Expr
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
