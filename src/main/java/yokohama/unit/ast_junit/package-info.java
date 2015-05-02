/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= Method*
 * 
 * Method ::= Annotation* Type* Type? Statement*
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
 *        | LongLitExpr
 *        | FloatLitExpr
 *        | DoubleLitExpr
 *        | BooleanLitExpr
 *        | CharLitExpr
 *        | ClassLitExpr
 *        | EqualOpExpr
 *        | ArrayExpr
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
 *             | ReturnStatement
 *             | InvokeVoidStatement
 *             | InvokeStaticVoidStatement
 * 
 * IsStatement ::= Var Var
 * IsNotStatement ::= Var Var
 * VarInitStatement ::= ClassType Expr
 * TryStatement ::= Statement* CatchClause* Statement*
 * IfStatement ::= Statement* Statement*
 * ReturnStatement ::= Var
 * InvokeVoidStatement ::= Var Type* Var*
 * InvokeStaticVoidStatement ::= Type* Type* Var*
 * 
 * CatchClause ::= ClassType Var Statement*
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;
