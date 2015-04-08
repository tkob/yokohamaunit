/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= Method*
 * 
 * Method ::= Statement*
 * 
 * Expr ::= VarExpr
 *        | InstanceOfMatcherExpr
 *        | NullValueMatcherExpr
 *        | EqualToMatcherExpr
 *        | NewExpr
 *        | StrLitExpr
 *        | NullExpr
 *        | InvokeExpr
 *        | InvokeStaticExpr
 *        | IntLitExpr
 *        | ClassLitExpr
 *        | EqualOpExpr
 * 
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * Statement ::= IsStatement
 *             | IsNotStatement
 *             | VarInitStatement
 *             | TryStatement
 *             | IfStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * VarInitStatement ::= ClassType Expr
 * TryStatement ::= Statement* CatchClause* Statement*
 * IfStatement ::= Statement* Statement*
 * 
 * CatchClause ::= ClassType Var Statement*
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;
