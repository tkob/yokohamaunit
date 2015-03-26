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
 *        | EqualOpExpr
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
 *             | IfStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * VarInitStatement ::= ClassType Expr
 * ReturnIsStatement ::= Var Var
 * ReturnIsNotStatement ::= Var Var
 * InvokeVoidStatement ::= Var Var*
 * TryStatement ::= Statement* CatchClause* Statement*
 * IfStatement ::= Statement* Statement*
 * 
 * CatchClause ::= ClassType Var Statement*
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;
